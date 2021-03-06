/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.ues.fmocc.protocolos.adm;

import java.io.IOException;
import java.io.Serializable;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NamingException;
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

    private Properties properties;

    @PostConstruct
    public void init() {
        user = new UserLDAP();
        properties = new Properties();
        try {
            properties.load(FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/WEB-INF/adm.properties"));
        } catch (IOException ex) {
            Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public UserLDAP getUser() {
        return user;
    }

    public void setUser(UserLDAP user) {
        this.user = user;
    }

    public void iniciarSesion() {
        if(properties == null){
            addMessage(FacesMessage.SEVERITY_INFO, "No se han definido propiedades del proyecto", "");
            return;
        }
        //Se agrega el DN completo del user admin de la sesion
        user.dn(properties.getProperty("admDN").replace("$CN", user.getCn()));//"cn=admin,dc=atol,dc=com"

        try {
            //Si no genera instancias de excepciones es las credenciales son correctas
            singleLDAP = new SingleLDAP(properties, user.dn(), user.getUserPassword());
            HttpSession session = SessionUtils.getSession();
            session.setAttribute("userDn", user.dn());
            session.setAttribute("userPassword", user.getUserPassword());
            redirect("/index.xhtml");
            singleLDAP.getContext().close();
            user = new UserLDAP();

        } catch (NamingException ex) {
            if (ex instanceof CommunicationException) {
                Logger.getLogger(LogInView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error al comunicarse con el host", "");

            } else if (ex instanceof AuthenticationException) {
                //Logger.getLogger(LogInView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Credenciales Invalidas", "");

            } else {
                Logger.getLogger(LogInView.class.getName()).log(Level.SEVERE, null, ex);
                addMessage(FacesMessage.SEVERITY_ERROR, "Error no definido", "");

            }
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
