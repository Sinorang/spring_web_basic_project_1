package com.elice.boardproject.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ReflectionUtils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class LoggingAspectTest {

    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    private TestService testService;

    @BeforeEach
    void setUp() {
        // 로그 캡처를 위해 System.out을 임시로 변경
        outContent = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        // 실제 AOP 적용 전, Mock 서비스만 생성
        testService = new TestService();
    }

    @Test
    void testNormalExecution_logging() {
        // given
        String param = "hello";
        // when
        String result = testService.echo(param);
        // then
        String logs = outContent.toString();
        assertThat(result).isEqualTo(param);
        // (실제 Aspect 적용 후) 로그에 메서드명, 파라미터, 반환값, 실행 시간 등이 포함되어야 함
        // 예시: "[LOG] Method: echo, Params: [hello], Result: hello, Time: ...ms"
        // (지금은 실패해야 정상)
        assertThat(logs).contains("echo");
        assertThat(logs).contains(param);
    }

    @Test
    void testException_logging() {
        // given
        // when
        Throwable thrown = catchThrowable(() -> testService.throwException());
        // then
        String logs = outContent.toString();
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        // (실제 Aspect 적용 후) 로그에 예외 메시지가 포함되어야 함
        assertThat(logs).contains("throwException");
        assertThat(logs).contains("IllegalStateException");
    }

    // Mock 서비스 클래스 (실제 서비스 계층 대체)
    static class TestService {
        public String echo(String input) {
            return input;
        }
        public void throwException() {
            throw new IllegalStateException("테스트 예외");
        }
    }
} 