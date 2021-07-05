/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.ues.fmocc.protocolos.adm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import sv.ues.fmocc.protocolos.adm.entity.UserLDAP;
import sv.ues.fmocc.protocolos.adm.utils.SessionUtils;
import sv.ues.fmocc.protocolos.adm.utils.SingleLDAP;

/**
 *
 * @author jcpleitez
 */
@Named
@ViewScoped
public class LogInView implements Serializable {

    private UserLDAP user;
    private SingleLDAP singleLDAP;

    @PostConstruct
    public void init() {
        user = new UserLDAP();
    }

    public UserLDAP getUser() {
        return user;
    }

    public void setUser(UserLDAP user) {
        this.user = user;
    }

    public void iniciarSesion() {
        user.setDn("cn="+user.getCn()+",dc=atol,dc=com");//"cn=admin,dc=atol,dc=com"
        
        singleLDAP = new SingleLDAP(user.getDn(), user.getUserPassword());
        if (singleLDAP.getContext() != null) {
            HttpSession session = SessionUtils.getSession();
            session.setAttribute("userDn", user.getDn());
            session.setAttribute("userPassword", user.getUserPassword());
            redirect("/index.xhtml");
        } else {
            addMessage(FacesMessage.SEVERITY_WARN, "Conexion a la LDAP", "Host o Credenciales incorrectas");
        }
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }
    
    public void redirect(String url) {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        try {
            context.redirect(context.getRequestContextPath() + url);
        } catch (IOException ex) {
            Logger.getLogger(this.getClass().getSimpleName()).log(Level.SEVERE, null, ex);
        }
    }

}
