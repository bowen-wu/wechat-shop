create table SHOP
(
    ID            BIGINT PRIMARY KEY AUTO_INCREMENT,
    NAME          VARCHAR(100),
    DESCRIPTION   VARCHAR(1024),
    IMG_URL       VARCHAR(1024),
    OWNER_USER_ID BIGINT,
    STATUS        VARCHAR(16),
    CREATED_AT    TIMESTAMP NOT NULL DEFAULT NOW(),
    UPDATED_AT    TIMESTAMP NOT NULL DEFAULT NOW()
)
