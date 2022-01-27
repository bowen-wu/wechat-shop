INSERT INTO USER (NAME, TEL, AVATAR_URL, CREATED_AT, UPDATED_AT)
VALUES ('user1', '13700001234', 'http://url', NOW(), NOW());
INSERT INTO USER (NAME, TEL, AVATAR_URL, CREATED_AT, UPDATED_AT)
VALUES ('user2', '13712345678', 'http://url', NOW(), NOW());
INSERT INTO USER (NAME, TEL, AVATAR_URL, CREATED_AT, UPDATED_AT)
VALUES ('user3', '13700000000', 'http://url', NOW(), NOW());

INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('李宁', '一切皆有可能', 'http://url', 1, 'ok', NOW(), NOW());
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('韦德之道', '一切皆有可能', 'http://url', 1, 'ok', NOW(), NOW());
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('安踏', '永不止步', 'http://url', 2, 'ok', NOW(), NOW());
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('FILA', '永不止步', 'http://url', 2, 'ok', NOW(), NOW());
INSERT INTO SHOP (NAME, DESCRIPTION, IMG_URL, OWNER_USER_ID, STATUS, CREATED_AT, UPDATED_AT)
VALUES ('乔丹体育', '乔丹体育', 'http://url', 2, 'deleted', NOW(), NOW());

INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, '上衣', 'This is beautiful t-shirt!', 'This is T-shirt details', 'http://url', 1000, 10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (1, '裤子', 'This is beautiful pants!', 'This is pants details', 'http://url', 1500, 10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (2, '紧身衣', 'This is beautiful Tights!', 'This is Tights details', 'http://url', 2000, 10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (3, '上衣', 'This is beautiful t-shirt!', 'This is T-shirt details', 'http://url', 1000, 10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (3, '裤子', 'This is beautiful pants!', 'This is pants details', 'http://url', 1500, 10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (4, '速干衣', 'This is beautiful Quick-drying clothes!', 'This is Quick-drying clothes details', 'http://url', 2000,
        10, 'ok', NOW(), NOW());
INSERT INTO GOODS (SHOP_ID, NAME, DESCRIPTION, DETAILS, IMAGE_URL, PRICE, STOCK, STATUS, CREATED_AT, UPDATED_AT)
VALUES (5, '速干衣', 'This is beautiful Quick-drying clothes!', 'This is Quick-drying clothes details', 'http://url', 20,
        1, 'ok', NOW(), NOW());
