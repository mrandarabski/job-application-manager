package nl.andarabski.testsupport.web;

import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public final class RestMatchers {
    private RestMatchers() {}

    public static ResultMatcher okResult() {
        return r -> {
            jsonPath("$.flag").value(true).match(r);
            jsonPath("$.code").value("SUCCESS").match(r);
        };
    }

    public static ResultMatcher createdResult() {
        return r -> {
            jsonPath("$.flag").value(true).match(r);
            jsonPath("$.code").value("CREATED").match(r);
        };
    }

    public static ResultMatcher notFoundResult() {
        return r -> {
            jsonPath("$.flag").value(false).match(r);
            jsonPath("$.code").value("NOT_FOUND").match(r);
        };
    }

    public static ResultMatcher badRequestResult() {
        return r -> {
            jsonPath("$.flag").value(false).match(r);
            jsonPath("$.code").value("INVALID_ARGUMENT").match(r);
        };
    }

    public static ResultMatcher conflictResult() {
        return r -> {
            jsonPath("$.flag").value(false).match(r);
            jsonPath("$.code").value("CONFLICT").match(r);
        };
    }

    public static ResultMatcher serverErrorResult() {
        return r -> {
            jsonPath("$.flag").value(false).match(r);
            jsonPath("$.code").value("INTERNAL_SERVER_ERROR").match(r);
        };
    }

}
