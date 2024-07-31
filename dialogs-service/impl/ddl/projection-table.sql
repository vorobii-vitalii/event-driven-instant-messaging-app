create table dialogs (
    dialog_id varchar(255) primary key,
    dialog_topic varchar(255) not null
);

create table dialog_participants (
    user_id varchar(255) not null,
    dialog_id varchar(255) not null
);

create table dialog_messages (
    message_id varchar(255) primary key,
    dialog_id varchar(255) not null,
    content varchar(500) not null,
    user_id varchar(255) not null,
    sent_at timestamp with time zone not null
);

create table dialog_messages_seen
(
    dialog_id  varchar(255) not null,
    message_id varchar(255) not null,
    user_id    varchar(255) not null
);
