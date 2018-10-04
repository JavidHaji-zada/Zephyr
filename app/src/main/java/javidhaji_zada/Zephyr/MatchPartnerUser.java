package javidhaji_zada.Zephyr;

import android.net.Uri;

public class MatchPartnerUser {
    public Uri image;
    public String ID;
    public String username;

    public MatchPartnerUser() {
    }

    public MatchPartnerUser(String ID, String username, Uri image) {
        this.ID = ID;
        this.username = username;
        this.image = image;
    }

    public String getID() {
        return ID;
    }

    public Uri getImage()
    {
        return image;
    }

    public String getUsername() {
        return username;
    }
}
