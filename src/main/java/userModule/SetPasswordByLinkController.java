package userModule;

import DAO.Implementations.PersonDAO;
import entitiesJPA.Person;
import log.SurveySystemLog;
import lombok.Getter;
import lombok.Setter;
import services.PasswordHash;
import services.SaltGenerator;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by arturas on 2017-04-02.
 *
 */
@Named
@javax.faces.view.ViewScoped
@SurveySystemLog
public class SetPasswordByLinkController implements Serializable
{
    @Inject private PersonDAO personDAO;
    @Getter @Setter Person person = new Person();

    @Setter @Getter private String unhashedPassword = null;
    @Getter @Setter private String confirmPassword = null;

    @Inject PasswordHash ph;
    @Inject SaltGenerator sg;

    public void validateInvitationLink(FacesContext context, UIComponent component, Object object)
    {
        String url = (String) object;
        //Tikrinam, ar toks URL yra duomenų bazėje
        if(isUrlInDatabase(url))
        {
            //tikrinam, ar vartotojas tikrai dar nera prisiregistraves
            if(person.getPassword() != null)
            {
                set400(context, "Vartotojas jau užsiregistravo");
            }
            //tikrinam, ar yra invite expiration data ir kiek galioja
            if(!isDateValid())
            {
                set400(context, "Nuoroda nebegalioja");
            }

        }
        else
        {
            set400(context, "Neteisingas URL");
        }
    }


    public void validatePasswordResetLink(FacesContext context, UIComponent component, Object object)
    {
        String url = (String) object;
        //Tikrinam, ar toks URL yra duomenų bazėje
        if(isUrlInDatabase(url))
        {
            //tikrinam, ar vartotojas yra prisiregistraves
            if(person.getPassword() == null)
            {
                set400(context, "Vartotojas dar neužsiregistravo");
            }
            //tikrinam, ar yra invite expiration data ir kiek galioja
            if(!isDateValid())
            {
                set400(context, "Nuoroda nebegalioja");
            }

        }
        else
        {
            set400(context, "Neteisingas URL");
        }
    }


    private boolean isUrlInDatabase(String url)
    {
        Person p = personDAO.FindPersonByInviteUrl(url);
        if(p == null)
        {
            return false;
        }
        else
        {
            this.person = p;
            return true;
        }
    }

    private boolean isDateValid()
    {
        Date currentDate = new Date();
        Date invitationDate = person.getInviteExpiration();
        if(invitationDate == null) return false;

        //nustatom datą, nuo kurios registracijos pakvietimas dar galiotų
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.DATE, -2);

        //tikrinam, ar pakvietimo data yra po tos datos, nuo kurios pakvietimas dar galiotų
        if(invitationDate.after(cal.getTime()))
        {
            return true;
        }
        else
        {
            return false;
        }

    }

    public String finishRegistration()
    {
        person.setPassword(ph.hashPassword(unhashedPassword));
        person.setInviteExpiration(null);
        personDAO.UpdateUser(person);
        return "/signin/signin?faces-redirect=true";
    }



    //Funkcija HTTP 400 Error kvietimui
    private void set400(FacesContext context, String message)
    {
        try
        {
            context.getExternalContext().responseSendError(400, message);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        context.responseComplete();
    }
}