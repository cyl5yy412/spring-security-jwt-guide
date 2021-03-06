package github.javaguide.springsecurityjwtguide.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.javaguide.springsecurityjwtguide.security.constants.SecurityConstants;
import github.javaguide.springsecurityjwtguide.security.entity.JwtUser;
import github.javaguide.springsecurityjwtguide.security.entity.LoginUser;
import github.javaguide.springsecurityjwtguide.security.utils.JwtTokenUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录验证
 *
 * @author Chr.yl
 *         如果用户名和密码正确，那么过滤器将创建一个JWT Token 并在HTTP Response 的header中返回它，格式：token: "Bearer +具体token值"
 */
public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private ThreadLocal<Boolean> rememberMe = new ThreadLocal<>();
    private AuthenticationManager authenticationManager;

    public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        // 设置URL，以确定是否需要身份验证
        super.setFilterProcessesUrl(SecurityConstants.AUTH_LOGIN_URL);//自定义登录路径
        /**
         * 注意:登录接口为json接口,form-data不可,且,记住我选择true,可返回票据
         {
         "username":"123456",
         "password":"123456",
         "rememberMe":true
         }
         */
    }

    /**
     * 1.捕获用户提交的内容,通过authenticate()赋值给authentication
     *
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            /**
             * com.fasterxml.jackson.databind.exc.MismatchedInputException: No content to map due to end-of-input
             at [Source: (org.apache.catalina.connector.CoyoteInputStream); line: 1, column: 0]
             */
            // 从流中获取到登录的信息:捕获用户提交的内容
            LoginUser loginUser = objectMapper.readValue(request.getInputStream(), LoginUser.class);
            rememberMe.set(loginUser.getRememberMe());
            // 这部分和attemptAuthentication方法中的源码是一样的，
            // 只不过由于这个方法源码的是把用户名和密码这些参数的名字是死的，所以我们重写了一下
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    loginUser.getUsername(), loginUser.getPassword());
            return authenticationManager.authenticate(authRequest);//返回捕获的内容
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 2.捕获结果处理加工,如果验证成功(登陆成功)，就生成token并返回
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication) {

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        List<String> roles = jwtUser.getAuthorities()//db获得roles
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        // 创建 Token
        String token = JwtTokenUtils.createToken(jwtUser.getUsername(), roles, rememberMe.get());
        // Http Response Header 中返回 Token
        response.setHeader(SecurityConstants.TOKEN_HEADER, token);
    }

    /**
     * 3.捕获结果处理加工,验证不成功,登录失败
     *
     * @param request
     * @param response
     * @param authenticationException
     * @throws IOException
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException authenticationException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authenticationException.getMessage());
    }
}
