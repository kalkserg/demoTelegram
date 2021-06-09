package ua.utilix.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.utilix.bot.ChatBot;

import java.util.ArrayList;

@Service
public class SendMessageService {

    private final ChatBot chatBot;

    public SendMessageService(ChatBot chatBot) {
        this.chatBot = chatBot;
    }

    public void test1(Message message) {
        var ms1 = SendMessage.builder()
                .text("<b>Bold</b> " +
                        "<i>italic</i >" +
                        " <code>mono</code> " +
                        "<a href=\"google.com\">Google</a>")
                .parseMode("HTML")
                .chatId(String.valueOf(message.getChatId()))
                .build();
        try {
            chatBot.execute(ms1);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void test2(Message message) {
        var markup = new ReplyKeyboardMarkup();
        var keyboardRows = new ArrayList<KeyboardRow>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add("Button1");
        row1.add("Button2");
        row1.add("Button3");
        row2.add(KeyboardButton.builder().text("Phone Number").requestContact(true).build());
        row3.add(KeyboardButton.builder().text("Location").requestLocation(true).build());
        keyboardRows.add(row1);
        keyboardRows.add(row2);
        keyboardRows.add(row3);
        markup.setKeyboard(keyboardRows);
        markup.setResizeKeyboard(true);
        //markup.setOneTimeKeyboard(true);
        SendMessage sendMessage = new SendMessage();
        if (message.hasText()) {
            sendMessage.setText(message.getText());
        } else {
            sendMessage.setText("aaaaaaaaaaaaaa");
        }
        System.out.println("1 " + sendMessage);
        System.out.println("2 " + message);
        System.out.println("");
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        sendMessage.setReplyMarkup(markup);
        try {
            chatBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void test3(SendMessage sendMessage) {
        System.out.println("1 " + sendMessage);
        System.out.println("");
        try {
            chatBot.execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
