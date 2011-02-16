package org.datacite.mds.web.ui.controller;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.datacite.mds.domain.Allocator;
import org.datacite.mds.domain.AllocatorOrDatacentre;
import org.datacite.mds.service.MagicAuthStringService;
import org.datacite.mds.test.Utils;
import org.datacite.mds.util.SecurityUtils;
import org.datacite.mds.web.ui.model.ChangePasswordModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/spring/applicationContext.xml")
@Transactional
public class ChangePasswordControllerTest {

    String symbol = "AL";
    String oldPassword = "old password";
    String newPassword = "new password";
    
    AllocatorOrDatacentre user;

    ChangePasswordController controller;

    @Autowired
    MagicAuthStringService magicAuthStringService;

    @Autowired
    PasswordEncoder passwordEncoder;
    
    String auth;
    ChangePasswordModel changePasswordModel;
    Model model;
    HttpServletRequest request;
    BindingResult result;

    @Before
    public void init() {
        controller = new ChangePasswordController();
        controller.magicAuthStringService = magicAuthStringService;
        controller.passwordEncoder = passwordEncoder;
        controller.authenticationManager = new AuthenticationManagerStub();

        user = Utils.createAllocator(symbol);
        user.setPassword(oldPassword);
        user.persist();

        auth = magicAuthStringService.getCurrentAuthString(user);

        model = new ExtendedModelMap();
        request = new MockHttpServletRequest();

        changePasswordModel = new ChangePasswordModel();
        changePasswordModel.setFirst(newPassword);
        changePasswordModel.setSecond(newPassword);

        result = new BeanPropertyBindingResult(changePasswordModel, "");
    }

    @Test
    public void createForm() {
        String view = controller.createForm(symbol, auth, model);
        Assert.assertEquals("password/change", view);
    }

    @Test
    public void createFormInvalidAuth() {
        String auth = "invalid auth";
        String view = controller.createForm(symbol, auth, model);
        Assert.assertEquals("password/expired", view);
    }

    @Test
    public void createFormUnknownSymbol() {
        AllocatorOrDatacentre unknown_user = Utils.createAllocator("UNKNOWN");
        String auth = magicAuthStringService.getCurrentAuthString(unknown_user);
        String symbol = unknown_user.getSymbol();
        String view = controller.createForm(symbol, auth, model);
        Assert.assertEquals("password/expired", view);
    }

    @Test
    public void changePasswordWhenNotLoggedIn() {
        Utils.login(null);
        changePassword();
        Assert.assertEquals(symbol, SecurityUtils.getCurrentSymbol());
    }

    @Test
    public void changePasswordWhenLoggedIn() {
        String symbol = "ANOTHER";
        Allocator anotherUser = Utils.createAllocator(symbol);
        Utils.login(anotherUser);
        
        changePassword();
        
        Assert.assertEquals(symbol, SecurityUtils.getCurrentSymbol());
    }

    private void changePassword() {
        String view = controller.changePassword(changePasswordModel, result, symbol, auth, model, request);
        Assert.assertEquals("password/success", view);

        String expectedPassword = passwordEncoder.encodePassword(newPassword, null);
        Assert.assertEquals(expectedPassword, user.getPassword());
    }

    @Test
    public void changePasswordInvalidAuth() {
        String auth = "invalid auth";
        String view = controller.changePassword(changePasswordModel, result, symbol, auth, model, request);
        Assert.assertEquals("password/expired", view);
    }

    @Test
    public void changePasswordUnknownSymbol() {
        AllocatorOrDatacentre unknown_user = Utils.createAllocator("UNKNOWN");
        String auth = magicAuthStringService.getCurrentAuthString(unknown_user);
        String symbol = unknown_user.getSymbol();
        String view = controller.changePassword(changePasswordModel, result, symbol, auth, model, request);
        Assert.assertEquals("password/expired", view);
    }

    class AuthenticationManagerStub implements AuthenticationManager {
        @Override
        public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            return authentication;
        }

    }

}
