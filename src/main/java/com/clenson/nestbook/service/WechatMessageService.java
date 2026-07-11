package com.clenson.nestbook.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clenson.nestbook.config.NestbookProperties;
import com.clenson.nestbook.config.WechatOaProperties;
import com.clenson.nestbook.entity.BillEntity;
import com.clenson.nestbook.entity.FamilyEntity;
import com.clenson.nestbook.entity.FamilyMemberEntity;
import com.clenson.nestbook.entity.UserEntity;
import com.clenson.nestbook.entity.WechatMessageLogEntity;
import com.clenson.nestbook.mapper.BillMapper;
import com.clenson.nestbook.mapper.FamilyMapper;
import com.clenson.nestbook.mapper.FamilyMemberMapper;
import com.clenson.nestbook.mapper.UserMapper;
import com.clenson.nestbook.mapper.WechatMessageLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Service
@Slf4j
@RequiredArgsConstructor
public class WechatMessageService {

    private static final Duration MESSAGE_TTL = Duration.ofDays(7);
    private static final int MAX_TEXT_LENGTH = 512;
    private static final List<String> COMMANDS = List.of("帮助", "绑定", "删除", "本月", "月报");

    private final WechatOaProperties wechatOaProperties;
    private final NestbookProperties nestbookProperties;
    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;
    private final FamilyMapper familyMapper;
    private final FamilyMemberMapper familyMemberMapper;
    private final BillMapper billMapper;
    private final WechatMessageLogMapper wechatMessageLogMapper;
    private final RuleBillParser ruleBillParser;

    public boolean isValidSignature(String signature, String timestamp, String nonce) {
        if (isBlank(wechatOaProperties.getToken()) || isBlank(signature) || isBlank(timestamp) || isBlank(nonce)) {
            return false;
        }
        String[] parts = {wechatOaProperties.getToken(), timestamp, nonce};
        Arrays.sort(parts);
        return MessageDigest.isEqual(sha1(String.join("", parts)).getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));
    }

    @Transactional
    public String handleOaMessage(String rawXml) {
        WechatXmlMessage message = parseMessage(rawXml);
        log.info("WeChat message received: type={}, openid={}, msgId={}", message.msgType(),
                mask(message.fromUserName()), message.msgId());
        if (!"text".equals(message.msgType())) {
            log.info("WeChat message ignored: unsupportedType={}, openid={}, msgId={}", message.msgType(),
                    mask(message.fromUserName()), message.msgId());
            return reply(message, "暂时仅支持发送文本记账。");
        }
        if (isBlank(message.fromUserName()) || isBlank(message.msgId()) || message.fromUserName().length() > 128
                || message.msgId().length() > 128) {
            log.warn("WeChat message rejected: invalid identity fields");
            return reply(message, "消息格式不正确，请重新发送。");
        }

        String responseKey = responseKey(message.fromUserName(), message.msgId());
        String cachedResponse = stringRedisTemplate.opsForValue().get(responseKey);
        if (cachedResponse != null) {
            log.info("WeChat message replayed from response cache: openid={}, msgId={}",
                    mask(message.fromUserName()), message.msgId());
            return reply(message, cachedResponse);
        }

        String processingKey = processingKey(message.fromUserName(), message.msgId());
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(processingKey, "1", MESSAGE_TTL);
        if (!Boolean.TRUE.equals(acquired)) {
            log.warn("WeChat message is already processing: openid={}, msgId={}",
                    mask(message.fromUserName()), message.msgId());
            return reply(message, "这条消息正在处理中，请稍后再试。");
        }

        try {
            Optional<WechatMessageLogEntity> processed = findMessageLog(message.fromUserName(), message.msgId());
            if (processed.isPresent()) {
                log.info("WeChat message deduplicated by database log: openid={}, msgId={}",
                        mask(message.fromUserName()), message.msgId());
                return reply(message, "这条消息已处理，请勿重复发送。");
            }

            ProcessingResult result = processMessage(message);
            writeMessageLog(message, result);
            stringRedisTemplate.opsForValue().set(responseKey, result.response(), MESSAGE_TTL);
            log.info("WeChat message processed: handleType={}, status={}, openid={}, msgId={}",
                    result.handleType(), result.status(), mask(message.fromUserName()), message.msgId());
            return reply(message, result.response());
        } catch (RuntimeException ex) {
            stringRedisTemplate.delete(processingKey);
            log.error("WeChat message processing failed: openid={}, msgId={}",
                    mask(message.fromUserName()), message.msgId(), ex);
            throw ex;
        }
    }

    private ProcessingResult processMessage(WechatXmlMessage message) {
        String content = message.content() == null ? "" : message.content().trim();
        if (content.isBlank() || content.length() > MAX_TEXT_LENGTH) {
            log.warn("WeChat message rejected: invalid text length, openid={}, msgId={}",
                    mask(message.fromUserName()), message.msgId());
            return ProcessingResult.failed("请输入不超过 512 个字符的记账内容。");
        }

        UserFamily userFamily = findOrCreateUserFamily(message.fromUserName());
        if (COMMANDS.contains(content)) {
            log.info("WeChat command recognized: command={}, openid={}, msgId={}", content,
                    mask(message.fromUserName()), message.msgId());
            return ProcessingResult.command(commandResponse(content));
        }

        if (!userFamily.family().getActivated() && userFamily.family().getTrialEndTime().isBefore(LocalDateTime.now())) {
            log.warn("WeChat recording blocked by trial expiration: familyId={}, openid={}, msgId={}",
                    userFamily.family().getId(), mask(message.fromUserName()), message.msgId());
            return ProcessingResult.failed("体验期已结束，请激活后继续记账。");
        }

        Optional<RuleBillParser.ParsedBill> parsed = ruleBillParser.parse(content);
        if (parsed.isEmpty()) {
            log.info("WeChat message did not match rule parser: openid={}, msgId={}",
                    mask(message.fromUserName()), message.msgId());
            return ProcessingResult.failed("暂不支持这条记账格式。请发送：早餐25，或：啤酒收入500。");
        }

        RuleBillParser.ParsedBill billData = parsed.get();
        BillEntity bill = new BillEntity();
        bill.setFamilyId(userFamily.family().getId());
        bill.setUserId(userFamily.user().getId());
        bill.setOaOpenid(message.fromUserName());
        bill.setBillType(billData.billType());
        bill.setAmount(billData.amount());
        bill.setCategory(billData.category());
        bill.setBillDate(LocalDate.now());
        bill.setRemark(billData.category());
        bill.setRawText(content);
        bill.setWechatMsgId(message.msgId());
        bill.setSource("wechat_oa");
        bill.setCreateTime(LocalDateTime.now());
        bill.setDeleted(false);
        billMapper.insert(bill);
        log.info("Bill recorded from WeChat: billId={}, familyId={}, userId={}, billType={}, msgId={}", bill.getId(),
                bill.getFamilyId(), bill.getUserId(), bill.getBillType(), message.msgId());

        String type = billData.billType() == 1 ? "收入" : "支出";
        return ProcessingResult.recorded("已记录" + type + "：" + billData.category() + " "
                + billData.amount().setScale(2) + " 元。");
    }

    private UserFamily findOrCreateUserFamily(String oaOpenid) {
        UserEntity user = userMapper.selectOne(new LambdaQueryWrapper<UserEntity>()
                .eq(UserEntity::getOaOpenid, oaOpenid));
        if (user == null) {
            user = new UserEntity();
            user.setOaOpenid(oaOpenid);
            user.setNickname("微信用户");
            user.setRole("member");
            user.setStatus("active");
            user.setCreateTime(LocalDateTime.now());
            userMapper.insert(user);
            log.info("WeChat user created: userId={}, openid={}", user.getId(), mask(oaOpenid));
        }

        FamilyMemberEntity member = familyMemberMapper.selectOne(new LambdaQueryWrapper<FamilyMemberEntity>()
                .eq(FamilyMemberEntity::getUserId, user.getId()));
        if (member == null) {
            FamilyEntity family = new FamilyEntity();
            family.setName("我的家庭");
            family.setCreatorUserId(user.getId());
            family.setCreateTime(LocalDateTime.now());
            family.setTrialEndTime(LocalDateTime.now().plusDays(nestbookProperties.getTrialDays()));
            family.setActivated(false);
            family.setDailyAiLimit(nestbookProperties.getDefaultDailyAiLimit());
            family.setStatus("active");
            familyMapper.insert(family);

            member = new FamilyMemberEntity();
            member.setFamilyId(family.getId());
            member.setUserId(user.getId());
            member.setNickname(user.getNickname());
            member.setRole("owner");
            member.setStatus("active");
            member.setJoinTime(LocalDateTime.now());
            member.setCreateTime(LocalDateTime.now());
            familyMemberMapper.insert(member);
            log.info("Family context created for WeChat user: familyId={}, userId={}", family.getId(), user.getId());
            return new UserFamily(user, family);
        }

        FamilyEntity family = familyMapper.selectById(member.getFamilyId());
        if (family == null || !"active".equals(family.getStatus())) {
            throw new IllegalStateException("Active family is required for a family member");
        }
        return new UserFamily(user, family);
    }

    private void writeMessageLog(WechatXmlMessage message, ProcessingResult result) {
        WechatMessageLogEntity log = new WechatMessageLogEntity();
        log.setOaOpenid(message.fromUserName());
        log.setWechatMsgId(message.msgId());
        log.setRawText(message.content());
        log.setMessageType(message.msgType());
        log.setHandleType(result.handleType());
        log.setHandleStatus(result.status());
        log.setErrorMessage(result.status().equals("failed") ? result.response() : null);
        log.setCreateTime(LocalDateTime.now());
        wechatMessageLogMapper.insert(log);
    }

    private Optional<WechatMessageLogEntity> findMessageLog(String oaOpenid, String msgId) {
        return Optional.ofNullable(wechatMessageLogMapper.selectOne(new LambdaQueryWrapper<WechatMessageLogEntity>()
                .eq(WechatMessageLogEntity::getOaOpenid, oaOpenid)
                .eq(WechatMessageLogEntity::getWechatMsgId, msgId)));
    }

    private String commandResponse(String command) {
        return switch (command) {
            case "帮助" -> "记账格式：早餐25 默认支出；啤酒收入500 为收入。";
            case "绑定" -> "小程序绑定功能即将开放，请稍后再试。";
            case "删除" -> "删除功能即将开放，请稍后再试。";
            case "本月" -> "本月统计功能即将开放，请稍后再试。";
            case "月报" -> "月报功能即将开放，请稍后再试。";
            default -> throw new IllegalArgumentException("Unknown command");
        };
    }

    private WechatXmlMessage parseMessage(String rawXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
            Element root = document.getDocumentElement();
            return new WechatXmlMessage(
                    text(root, "ToUserName"), text(root, "FromUserName"), text(root, "CreateTime"),
                    text(root, "MsgType"), text(root, "MsgId"), text(root, "Content")
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid WeChat XML message", ex);
        }
    }

    private String reply(WechatXmlMessage message, String content) {
        return "<xml><ToUserName><![CDATA[" + escapeCdata(message.fromUserName()) + "]]></ToUserName>"
                + "<FromUserName><![CDATA[" + escapeCdata(message.toUserName()) + "]]></FromUserName>"
                + "<CreateTime>" + (System.currentTimeMillis() / 1000) + "</CreateTime>"
                + "<MsgType><![CDATA[text]]></MsgType><Content><![CDATA[" + escapeCdata(content)
                + "]]></Content></xml>";
    }

    private String text(Element root, String name) {
        var nodes = root.getElementsByTagName(name);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private String responseKey(String oaOpenid, String msgId) {
        return "nb:wechat:resp:" + oaOpenid + ":" + msgId;
    }

    private String processingKey(String oaOpenid, String msgId) {
        return "nb:wechat:msg:" + oaOpenid + ":" + msgId;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sha1(String raw) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                    .digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-1 is not available", ex);
        }
    }

    private String escapeCdata(String value) {
        return (value == null ? "" : value).replace("]]>", "]]]]><![CDATA[>");
    }

    private String mask(String value) {
        if (isBlank(value)) {
            return "<empty>";
        }
        if (value.length() <= 7) {
            return "***";
        }
        return value.substring(0, 3) + "***" + value.substring(value.length() - 4);
    }

    private record UserFamily(UserEntity user, FamilyEntity family) {
    }

    private record ProcessingResult(String handleType, String status, String response) {
        static ProcessingResult command(String response) {
            return new ProcessingResult("command", "success", response);
        }

        static ProcessingResult recorded(String response) {
            return new ProcessingResult("rule", "success", response);
        }

        static ProcessingResult failed(String response) {
            return new ProcessingResult("rule", "failed", response);
        }
    }
}
