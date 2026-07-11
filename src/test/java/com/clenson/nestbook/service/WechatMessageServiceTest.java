package com.clenson.nestbook.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WechatMessageServiceTest {

    private static final String MESSAGE = """
            <xml>
              <ToUserName><![CDATA[gh_nestbook]]></ToUserName>
              <FromUserName><![CDATA[oa_user_1]]></FromUserName>
              <CreateTime>1720000000</CreateTime>
              <MsgType><![CDATA[text]]></MsgType>
              <Content><![CDATA[支出 25 早餐]]></Content>
              <MsgId>msg_1</MsgId>
            </xml>
            """;

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private UserMapper userMapper;
    @Mock
    private FamilyMapper familyMapper;
    @Mock
    private FamilyMemberMapper familyMemberMapper;
    @Mock
    private BillMapper billMapper;
    @Mock
    private WechatMessageLogMapper wechatMessageLogMapper;

    private WechatMessageService service;

    @BeforeEach
    void setUp() {
        WechatOaProperties wechatProperties = new WechatOaProperties();
        wechatProperties.setToken("token-123");
        NestbookProperties nestbookProperties = new NestbookProperties();

        service = new WechatMessageService(wechatProperties, nestbookProperties, stringRedisTemplate, userMapper,
                familyMapper, familyMemberMapper, billMapper, wechatMessageLogMapper, new RuleBillParser());
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(wechatMessageLogMapper.selectOne(any())).thenReturn(null);
        when(userMapper.selectOne(any())).thenReturn(null);
        when(familyMemberMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ((UserEntity) invocation.getArgument(0)).setId(11L);
            return 1;
        }).when(userMapper).insert(any(UserEntity.class));
        doAnswer(invocation -> {
            ((FamilyEntity) invocation.getArgument(0)).setId(22L);
            return 1;
        }).when(familyMapper).insert(any(FamilyEntity.class));
    }

    @Test
    void validatesWechatSignature() throws Exception {
        String signature = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1")
                .digest("nonce-1timestamp-1token-123".getBytes(StandardCharsets.UTF_8)));

        assertThat(service.isValidSignature(signature, "timestamp-1", "nonce-1")).isTrue();
        assertThat(service.isValidSignature("wrong", "timestamp-1", "nonce-1")).isFalse();
    }

    @Test
    void appliesDefaultExpenseAndExplicitIncomeConventions() {
        RuleBillParser parser = new RuleBillParser();

        RuleBillParser.ParsedBill breakfast = parser.parse("早餐25").orElseThrow();
        RuleBillParser.ParsedBill beerIncome = parser.parse("啤酒收入500").orElseThrow();

        assertThat(breakfast.billType()).isEqualTo(2);
        assertThat(breakfast.category()).isEqualTo("早餐");
        assertThat(breakfast.amount()).isEqualByComparingTo("25");
        assertThat(beerIncome.billType()).isEqualTo(1);
        assertThat(beerIncome.category()).isEqualTo("啤酒");
        assertThat(beerIncome.amount()).isEqualByComparingTo("500");
    }

    @Test
    void recordsStructuredWechatBillAndCreatesFamilyContext() {
        String response = service.handleOaMessage(MESSAGE);

        ArgumentCaptor<BillEntity> billCaptor = ArgumentCaptor.forClass(BillEntity.class);
        verify(billMapper).insert(billCaptor.capture());
        BillEntity bill = billCaptor.getValue();
        assertThat(bill.getFamilyId()).isEqualTo(22L);
        assertThat(bill.getUserId()).isEqualTo(11L);
        assertThat(bill.getBillType()).isEqualTo(2);
        assertThat(bill.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(25));
        assertThat(bill.getCategory()).isEqualTo("早餐");
        assertThat(response).contains("<![CDATA[已记录支出：早餐 25.00 元。]]>");
        verify(familyMemberMapper).insert(any(FamilyMemberEntity.class));
    }

    @Test
    void returnsCachedReplyWithoutWritingAnotherBill() {
        when(valueOperations.get("nb:wechat:resp:oa_user_1:msg_1")).thenReturn("已记录支出：早餐 25.00 元。");

        String response = service.handleOaMessage(MESSAGE);

        assertThat(response).contains("<![CDATA[已记录支出：早餐 25.00 元。]]>");
        verify(billMapper, never()).insert(any(BillEntity.class));
        verify(wechatMessageLogMapper, never()).insert(any(WechatMessageLogEntity.class));
    }

    @Test
    void recognizesCommandsBeforeBillParsing() {
        String response = service.handleOaMessage(MESSAGE.replace("支出 25 早餐", "帮助"));

        assertThat(response).contains("早餐25 默认支出；啤酒收入500 为收入。");
        verify(billMapper, never()).insert(any(BillEntity.class));
    }

    @Test
    void rejectsRecordingAfterTrialExpires() {
        UserEntity user = new UserEntity();
        user.setId(11L);
        user.setNickname("微信用户");
        FamilyMemberEntity member = new FamilyMemberEntity();
        member.setFamilyId(22L);
        FamilyEntity family = new FamilyEntity();
        family.setId(22L);
        family.setActivated(false);
        family.setStatus("active");
        family.setTrialEndTime(LocalDateTime.now().minusSeconds(1));
        when(userMapper.selectOne(any())).thenReturn(user);
        when(familyMemberMapper.selectOne(any())).thenReturn(member);
        when(familyMapper.selectById(22L)).thenReturn(family);

        String response = service.handleOaMessage(MESSAGE);

        assertThat(response).contains("体验期已结束，请激活后继续记账。");
        verify(billMapper, never()).insert(any(BillEntity.class));
    }
}
