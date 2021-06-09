package ua.utilix.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public enum BotState {

    Start {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Hello!");
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
        }

        @Override
        public BotState nextState() {
            return Registred;
        }
    },

    Registred {
        @Override
        public void enter(BotContext context) {
            sendMessage(context, "Registred!");
        }

        @Override
        public void handleInput(BotContext context) {
            context.getUser().setNotified(true);
        }

        @Override
        public BotState nextState() {

            return Done;
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

        return states[id];
    }

    protected void sendMessage(BotContext context, String text) {
        var message = SendMessage.builder()
                .chatId(String.valueOf(context.getUser().getChatId()))
                .text(text)
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
    }

    public abstract void enter(BotContext context);

    public abstract BotState nextState();
}
