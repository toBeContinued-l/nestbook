package com.clenson.nestbook;

import com.clenson.nestbook.core.response.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void okShouldUseStableSuccessCode() {
        ApiResponse<String> response = ApiResponse.ok("ready");

        assertThat(response.code()).isEqualTo("0");
        assertThat(response.data()).isEqualTo("ready");
    }
}

