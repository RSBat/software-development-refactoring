package ru.akirakozov.sd.refactoring.servlet;

import org.eclipse.jetty.util.IO;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DriverManager.class)
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

    @Test
    public void whenQueryIsSuccessfulThenSqlQueryIsMade() throws IOException, SQLException {
        HttpServletRequest request = createRequestMock("test", "0");
        HttpServletResponse response = createResponseMock();

        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class);

        when(DriverManager.getConnection(any())).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);

        servlet.doGet(request, response);

        verify(statement).executeUpdate(createSqlQuery("test", 0));

        driverManager.close();
    }

    @Test
    public void whenSqlQueryFailsExceptionIsRethrown() throws IOException, SQLException {
        HttpServletRequest request = createRequestMock("test", "0");
        HttpServletResponse response = createResponseMock();

        Connection connection = mock(Connection.class);
        MockedStatic<DriverManager> driverManager = mockStatic(DriverManager.class);

        when(DriverManager.getConnection(any())).thenReturn(connection);
        when(connection.createStatement()).thenThrow(new RuntimeException());

        // TODO check cause
        assertThrows(RuntimeException.class, () -> servlet.doGet(request, response));

        driverManager.close();
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

    private String createSqlQuery(String name, long price) {
        return "INSERT INTO PRODUCT " +
                "(NAME, PRICE) VALUES (\"" + name + "\"," + price + ")";
    }
}