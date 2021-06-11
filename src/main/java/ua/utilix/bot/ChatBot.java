package ua.utilix.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.utilix.model.User;
import ua.utilix.service.UserService;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:telegram.properties")
public class ChatBot extends TelegramLongPollingBot {

    private static final Logger LOGGER = LogManager.getLogger(ChatBot.class);

    private static final String BROADCAST = "broadcast ";
    private static final String LIST_USERS = "users";

    public static final String ADD_REQUEST = "Add";
    public static final String REMOVE_REQUEST = "Del";

    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String botToken;

    private final UserService userService;

    public ChatBot(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText())
            return;

        final String text = update.getMessage().getText();
        final long chatId = update.getMessage().getChatId();

        User user = null;

        User[] users = userService.findByChatId(chatId);
        try {
            user = users[users.length-1];
        }catch (Exception e){}

        //if(user != null) user =
        findUserWithNullSigfoxName(users);

        if (checkIfAdminCommand(user, text))
            return;

        BotContext context;
        BotState state;

        System.out.println(update.getMessage().getText());

        if (user == null) {
            state = BotState.getInitialState();

            user = new User(chatId, text, state.ordinal());
            userService.addUser(user);

            context = BotContext.of(this, user, text);
            state.enter(context);

            LOGGER.info("New user registered: " + chatId);
        } else if (update.getMessage().getText().equals(ADD_REQUEST)) {
            state = BotState.Start;
            //state = BotState.getInitialState();

            user = new User(chatId, state.ordinal());
            userService.addUser(user);

            context = BotContext.of(this, user, text);
            state.enter(context);

            LOGGER.info("Add id registered: " + chatId);
        } else {
            context = BotContext.of(this, user, text);
            state = BotState.byId(user.getStateId());

            LOGGER.info("Update received for user in state: " + state);
        }

        state.handleInput(context);

        do {
            state = state.nextState();
            state.enter(context);
        } while (!state.isInputNeeded());

        user.setStateId(state.ordinal());
        userService.updateUser(user);
    }




    private boolean checkIfAdminCommand(User user, String text) {
        try {
            System.out.println("admin " + user.toString());
        }catch (Exception e){}

        if (user == null || !user.getAdmin())
            return false;

        if (text.startsWith(BROADCAST)) {
            LOGGER.info("Admin command received: " + BROADCAST);

            text = text.substring(BROADCAST.length());
            broadcast(text);

            return true;
        } else if (text.equals(LIST_USERS)) {
            LOGGER.info("Admin command received: " + LIST_USERS);

            listUsers(user);
            return true;
        }

        return false;
    }

    private void sendMessage(String chatId, String text) {
        var message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(getMainMenu())
                .build();
        message.setParseMode("HTML");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void listUsers(User admin) {
        StringBuilder sb = new StringBuilder("All users list:\r\n");
        List<User> users = userService.findAllUsers();

        users.forEach(user ->
            sb.append(user.getId())
                    .append(" - <b>")
                    .append(user.getSigfoxName())
                    .append("</b> id: ")
                    .append(user.getSigfoxId())
                    .append("\r\n")
        );

        sendMessage(String.valueOf(admin.getChatId()), sb.toString());
    }

    private void broadcast(String text) {
        List<User> users = userService.findAllUsers();
        users.forEach(user -> sendMessage(String.valueOf(user.getChatId()), text));
    }

    protected ReplyKeyboardMarkup getMainMenu(){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        KeyboardRow row1 = new KeyboardRow();
        row1.add(ADD_REQUEST);
        row1.add(REMOVE_REQUEST);

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row1);
        markup.setKeyboard(rows);
        markup.setResizeKeyboard(true);
        return markup;
    }

    private User findUserWithNullSigfoxName(User[] users) {
        for (int i = 0; i < users.length; i++) {
            System.out.println(i + " " + users[i].getChatId() + " " + users[i].getSigfoxName() + " " + users[i].getSigfoxId() +" " + users[i].getStateId());
            if (users[i].getSigfoxName() == null)
                return users[i];
        }
        return null;
    }
}
