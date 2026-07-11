package com.clenson.nestbook.controller.api.wechat;

import com.clenson.nestbook.service.WechatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/wechat/oa/callback")
public class WechatOaController {

    private final WechatMessageService wechatMessageService;

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(defaultValue = "") String signature,
            @RequestParam(defaultValue = "") String timestamp,
            @RequestParam(defaultValue = "") String nonce,
            @RequestParam(defaultValue = "") String echostr
    ) {
        if (!wechatMessageService.isValidSignature(signature, timestamp, nonce)) {
            log.warn("WeChat callback signature rejected: method=GET");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("WeChat callback verified: method=GET");
        return ResponseEntity.ok(echostr);
    }

    @PostMapping(consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE}, produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> receive(
            @RequestParam(defaultValue = "") String signature,
            @RequestParam(defaultValue = "") String timestamp,
            @RequestParam(defaultValue = "") String nonce,
            @RequestBody String rawXml
    ) {
        if (!wechatMessageService.isValidSignature(signature, timestamp, nonce)) {
            log.warn("WeChat callback signature rejected: method=POST, payloadLength={}", rawXml.length());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        log.info("WeChat callback accepted: method=POST, payloadLength={}", rawXml.length());
        String response = wechatMessageService.handleOaMessage(rawXml);
        log.info("WeChat callback completed: method=POST, responseLength={}", response.length());
        return ResponseEntity.ok(response);
    }
}
