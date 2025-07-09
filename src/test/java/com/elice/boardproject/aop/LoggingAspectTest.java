package com.elice.boardproject.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.TestPropertySource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.elice.boardproject.service.TestService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.elice.boardproject.aop=INFO"
})
@Import(TestService.class)
public class LoggingAspectTest {

    @Autowired
    private TestService testService;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(LoggingAspect.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void testNormalExecution_logging() {
        // given
        String param = "hello";
        // when
        String result = testService.echo(param);
        // then
        assertThat(result).isEqualTo(param);
        // 로그 메시지 직접 출력
        listAppender.list.forEach(event -> System.out.println("[LOG] " + event.getFormattedMessage()));
        boolean found = listAppender.list.stream().anyMatch(event ->
                event.getFormattedMessage().contains("Method: echo")
        );
        assertThat(found).isTrue();
    }

    @Test
    void testException_logging() {
        // given
        // when
        Throwable thrown = catchThrowable(() -> testService.throwException());
        // then
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        // 로그 메시지 직접 출력
        listAppender.list.forEach(event -> System.out.println("[LOG] " + event.getFormattedMessage()));
        boolean found = listAppender.list.stream().anyMatch(event ->
                event.getFormattedMessage().contains("Method: throwException")
        );
        assertThat(found).isTrue();
    }

    @org.springframework.boot.test.context.TestConfiguration
    static class TestServiceConfig {
        @org.springframework.context.annotation.Bean
        public TestService testService() {
            return new TestService();
        }
    }
} 