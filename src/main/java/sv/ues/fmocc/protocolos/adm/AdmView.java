/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sv.ues.fmocc.protocolos.adm;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import sv.ues.fmocc.protocolos.adm.entity.UserLDAP;
import sv.ues.fmocc.protocolos.adm.utils.SessionUtils;
import sv.ues.fmocc.protocolos.adm.utils.SingleLDAP;

/**
 *
 * @author jcpleitez
 */
@Named
@ViewScoped
public class AdmView implements Serializable {

    private UserLDAP user;
    private SingleLDAP singleLDAP;

    @PostConstruct
    public void init() {
        user = new UserLDAP();
        
        singleLDAP = new SingleLDAP(SessionUtils.getUserUserDn(), SessionUtils.getUserPassword());
    }

    public UserLDAP getUser() {
        return user;
    }

    public void setUser(UserLDAP user) {
        this.user = user;
    }
    
    public void updateUID(){
        String uid = "";
        uid += user.getCn() == null ? "" : user.getCn();
        uid += ".";
        uid += user.getSn() == null ? "" : user.getSn();
        user.setUid(uid);
    }

    public void crearUsuario() {
        //user.setCn("");
        //user.setSn("");
        //user.setUserPassword("");
        //user.setUid("");
        user.setHomeDirectory("/home/vmail/atol/"+user.getUid());
        user.setMail(user.getUid()+"@atol.com");
        user.setMailbox("atol/"+user.getUid()+"/");
        
        if (user.isComplete()) {
            Attributes attributes = new BasicAttributes();
            Attribute attribute = new BasicAttribute("objectClass");
            attribute.add("inetOrgPerson");
            attribute.add("organizationalPerson");
            attribute.add("person");
            attribute.add("simpleSecurityObject");
            attribute.add("CourierMailAccount");
            attribute.add("top");

            attributes.put(attribute);
            // Se recorren todos los getter de la entidad para asignarlos
            Map<String, String> map = user.toMap();
            for (String key : map.keySet()) {
                attributes.put(key, map.get(key));
            }

            try {
                singleLDAP.getContext().createSubcontext("uid="+map.get("uid")+",ou=sistemas,ou=usuarios,dc=atol,dc=com", attributes);
            } catch (NamingException ex) {
                Logger.getLogger(AdmView.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
