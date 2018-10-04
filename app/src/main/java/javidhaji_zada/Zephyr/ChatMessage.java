package javidhaji_zada.Zephyr;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatMessage implements Comparable<ChatMessage>{

    // properties
    private String message;
    private String createdAt;
    private String sender;
    // constructor

    public String getCreatedAt() {
        return createdAt;
    }

    public String getSender() {
        return sender;
    }

    ChatMessage(String sender, String message, String createdAt) {
        this.message = message;
        this.createdAt = createdAt;
        this.sender = sender;

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int compareTo(@NonNull ChatMessage o) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
        try {
            Date date1 = sdf.parse(this.getCreatedAt());
            Date date2 = sdf.parse(o.getCreatedAt());
            if ( date1.after(date2))
            {
                return 1;
            }
            else if ( date1.before(date2))
            {
                return -1;
            }
        } catch (ParseException ignored) {
        }
        return 0;
    }
}

