package com.abarigena.authenticationservice.service;

import com.abarigena.authenticationservice.dto.*;
import org.springframework.stereotype.Service;


public interface AuthService {
    void register(RegisterDto registerDto);
    void confirmEmail(String token);
    TokenDto login(LoginDto loginDto);
    TokenDto refreshToken(RefreshDto refreshDto);
    void forgotPassword(ForgotPasswordDto forgotPasswordDto);
    void resetPassword(ResetPasswordDto resetPasswordDto);
    UserInfo validateToken(ValidateTokenDto validateTokenDto);
    void logout(LogoutDto logoutDto);
}
