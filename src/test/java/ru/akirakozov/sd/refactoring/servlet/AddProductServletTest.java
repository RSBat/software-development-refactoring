package ru.akirakozov.sd.refactoring.servlet;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddProductServletTest {
    private final AddProductServlet servlet = new AddProductServlet();
    private final StringWriter writer = new StringWriter();

    @Test
    public void whenRequestSuccessfulServletSetsContentType() throws IOException {
        HttpServletRequest request = createRequestMock("test", "0");
        HttpServletResponse response = createResponseMock();

        servlet.doGet(request, response);

        verify(response).setContentType("text/html");
    }

    @Test
    public void whenRequestSuccessfulServletSetsStatus() throws IOException {
        HttpServletRequest request = createRequestMock("test", "0");
        HttpServletResponse response = createResponseMock();

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void whenRequestSuccessfulServletReturnsOkText() throws IOException {
        HttpServletRequest request = createRequestMock("test", "0");
        HttpServletResponse response = createResponseMock();

        servlet.doGet(request, response);

        String result = writer.toString();
        assertEquals("OK\n", result);
    }

    @Test
    public void whenNoNameIsSetThenRequestIsSuccessful() throws IOException {
        HttpServletRequest request = createRequestMock(null, "0");
        HttpServletResponse response = createResponseMock();

        servlet.doGet(request, response);

        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void whenNoPriceIsSetThenRequestFailsWithNumberFormatException() throws IOException {
        HttpServletRequest request = createRequestMock("name", null);
        HttpServletResponse response = createResponseMock();

        assertThrows(NumberFormatException.class, () -> servlet.doGet(request, response));
        verify(response, never()).setStatus(anyInt());
    }

    private HttpServletRequest createRequestMock(String name, String price) {
        HttpServletRequest mock = mock(HttpServletRequest.class);
        when(mock.getParameter("name")).thenReturn(name);
        when(mock.getParameter("price")).thenReturn(price);

        return mock;
    }

    private HttpServletResponse createResponseMock() throws IOException {
        HttpServletResponse mock = mock(HttpServletResponse.class);
        when(mock.getWriter()).thenReturn(new PrintWriter(writer));

        return mock;
    }
}