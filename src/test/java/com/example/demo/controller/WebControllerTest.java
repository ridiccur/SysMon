package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WebControllerTest {

    WebController controller = new WebController();

    @Test
    void redirectToIndex_returnsRedirect() {
        String res = controller.redirectToDashboard();
        assertThat(res).isEqualTo("redirect:/index.html");
    }

    @Test
    void dashboard_forwardsToIndex() {
        String res = controller.dashboard();
        assertThat(res).isEqualTo("forward:/index.html");
    }
}
