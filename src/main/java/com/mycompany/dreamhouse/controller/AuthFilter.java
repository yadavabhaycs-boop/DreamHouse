package com.mycompany.dreamhouse.controller;
import jakarta.servlet.*; import jakarta.servlet.annotation.WebFilter; import jakarta.servlet.http.*; import java.io.IOException;
@WebFilter(urlPatterns={"/dashboard.html","/admin-dashboard.html","/generator.html","/saved-designs.html","/profile.html"})
public class AuthFilter implements Filter {
 @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain)throws IOException,ServletException{HttpServletRequest q=(HttpServletRequest)req;HttpServletResponse r=(HttpServletResponse)res;HttpSession s=q.getSession(false);String role=s==null?null:(String)s.getAttribute("role");if(role==null){r.sendRedirect(q.getContextPath()+"/login.html");return;}boolean adminPage=q.getRequestURI().endsWith("/admin-dashboard.html");if(adminPage&&!"ADMIN".equals(role)){r.sendError(403,"Admin access required.");return;}if(!adminPage&&"ADMIN".equals(role)){r.sendRedirect(q.getContextPath()+"/admin-dashboard.html");return;}chain.doFilter(req,res);}
}

