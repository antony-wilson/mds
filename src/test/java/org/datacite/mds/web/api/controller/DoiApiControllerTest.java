package org.datacite.mds.web.api.controller;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import javax.validation.ValidationException;

import org.datacite.mds.service.DoiService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

public class DoiApiControllerTest {

    DoiApiController doiApiController = new DoiApiController();
    DoiService mockDoiService;

    String doi = "10.5072/1111";
    String url = "http://example.com";

    @Before
    public void setUp() throws Exception {
        mockDoiService = createMock(DoiService.class);
        doiApiController.doiService = this.mockDoiService;
    }

    @Test
    public void testGetRoot() throws Exception {
        ResponseEntity response = doiApiController.getRoot();
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testPost() throws Exception {
        expectDoiServiceCreateOrUpdate();
        HttpStatus statusCode = post("doi=" + doi + "\nurl=" + url, false);
        assertEquals(HttpStatus.CREATED, statusCode);
    }

    @Test
    public void testPostSpecial() throws Exception {
        expectDoiServiceCreateOrUpdate();
        String body = String.format("url=foobar\n doi=%s\r\n Url=%s\n#doi=foobar", doi, url);
        HttpStatus statusCode = post(body, false);
        assertEquals(HttpStatus.CREATED, statusCode);
    }

    @Test(expected = ValidationException.class)
    public void testPostMissingDoi() throws Exception {
        post("doi=" + doi, false);
    }

    @Test(expected = ValidationException.class)
    public void testPostMissingUrl() throws Exception {
        post("url=" + url, false);
    }

    @Test
    public void testPut() throws Exception {
        expectDoiServiceCreateOrUpdate();
        HttpStatus statusCode = put(doi, "doi=" + doi + "\nurl=" + url, false);
        assertEquals(HttpStatus.CREATED, statusCode);
    }

    @Test(expected = HttpRequestMethodNotSupportedException.class)
    public void testPutNoDoi() throws Exception {
        doiApiController.putRoot();
    }
    
    @Test(expected = ValidationException.class)
    public void testPutMismatchingDoi() throws Exception {
        put(doi + "-wrong", "doi=" + doi + "\nurl=" + url, false);
    }
    
    @Test(expected = ValidationException.class)
    public void testPutMissingDoi() throws Exception {
        put(doi, "doi=" + doi, false);
    }

    @Test(expected = ValidationException.class)
    public void testPutMissingUrl() throws Exception {
        put(doi, "url=" + url, false);
    }

    private void expectDoiServiceCreateOrUpdate() throws Exception {
        expect(mockDoiService.createOrUpdate(eq(doi), eq(url), anyBoolean())).andStubReturn(null);
    }

    private MockHttpServletRequest makeServletRequestForDoi(String doi) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/doi/" + doi);
        return request;
    }

    private HttpStatus post(String body, Boolean testMode) throws Exception {
        MockHttpServletRequest httpRequest = makeServletRequestForDoi(null);
        httpRequest.setMethod("POST");
        replay(mockDoiService);
        ResponseEntity<? extends Object> response = doiApiController.post(body, testMode, httpRequest);
        verify(mockDoiService);
        return response.getStatusCode();
    }

    private HttpStatus put(String doi, String body, Boolean testMode) throws Exception {
        MockHttpServletRequest httpRequest = makeServletRequestForDoi(doi);
        httpRequest.setMethod("PUT");
        replay(mockDoiService);
        ResponseEntity<? extends Object> response = doiApiController.put(body, testMode, httpRequest);
        verify(mockDoiService);
        return response.getStatusCode();
    }

}
