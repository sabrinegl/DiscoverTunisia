package com.example.sabrine.discovertunisia.firebase;

public class FirebaseEntities {

    public interface Users {
        public static final String ENTITY_NAME = "Users";

        public static final String FIELD_ONLINE = "online";
        public static final String ONLINE_VALUE_AVAILABLE = "AVAILABLE";
        public static final String ONLINE_VALUE_BUSY = "BUSY";
        public static final String ONLINE_VALUE_DO_NOT_DISTURB = "DO_NOT_DISTURB";

        public static final String FIELD_NAME = "name";

        public static final String FIELD_THUMB = "thumb_image";
    }

    public interface Friends {
        public static final String ENTITY_NAME = "Friends";

    }

    public interface Chat {
        public static final String ENTITY_NAME = "Chat";
        public static final String FIELD_TIMESTAMP = "timestamp";

    }

    public interface Messages {
        public static final String ENTITY_NAME = "Messages";

    }
}
