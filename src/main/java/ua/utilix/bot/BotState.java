package ua.utilix.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Locale;

public enum BotState {

    Start {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Hello!");
        }

        @Override
        public BotState nextState() {
            return EnterSigfoxName;
        }
    },

    EnterSigfoxName {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter message from Sigfox Device please:");
        }

        @Override
        public void handleInput(BotContext context) {
            context.getUser().setSigfoxName(context.getInput());
        }

        @Override
        public BotState nextState() {
            return EnterSigfoxID;
        }
    },

    EnterSigfoxID {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Enter your Sigfox ID please:");
        }

        @Override
        public void handleInput(BotContext context) {
            context.getUser().setSigfoxId(context.getInput());
            context.getUser().setNotified(true);
            sendMessage(context, "Ok? Yes:No");
        }

        @Override
        public BotState nextState() {
            return Registred;
        }
    },

    Registred {
        private BotState next;
        @Override
        public void enter(BotContext context) {
            //sendMessage(context, "Ok? Yes:No");
        }

        @Override
        public void handleInput(BotContext context) {
            //System.out.println("conte " + context.getInput());
            if (context.getInput().toLowerCase(Locale.ROOT).contains("y")) {
                next = Done;
                sendMessage(context, "Registred!");
            } else {
                next = EnterSigfoxName;
                sendMessage(context, "Begin");
            }
        }

        @Override
        public BotState nextState() {
            return next;
        }
    },

    Done {
        @Override
        public void enter(BotContext context) {
        }

        @Override
        public BotState nextState() {
            return Done;
        }
    };

    private static BotState[] states;
    private final boolean inputNeeded;

    BotState() {
        this.inputNeeded = true;
    }

    BotState(boolean inputNeeded) {
        this.inputNeeded = inputNeeded;
    }

    public static BotState getInitialState() {
        return byId(0);
    }

    public static BotState byId(int id) {
        if (states == null) {
            states = BotState.values();
        }
        System.out.println("state "+states[id] + " id " + id);
        return states[id];
    }

    protected void sendMessage(BotContext context, String text) {
        var message = SendMessage.builder()
                .chatId(String.valueOf(context.getUser().getChatId()))
                .text(text)
                .replyMarkup(context.getBot().getMainMenu())
                .build();
        try {
            context.getBot().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean isInputNeeded() {
        return inputNeeded;
    }

    public void handleInput(BotContext context) {
        // do nothing by default
        System.out.println("need next");
    }

    public abstract void enter(BotContext context);

    public abstract BotState nextState();
}
