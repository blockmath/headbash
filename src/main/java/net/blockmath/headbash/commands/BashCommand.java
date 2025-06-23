package net.blockmath.headbash.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.blockmath.headbash.commands.arguments.WeirdgeDoubleArgumentType;
import net.blockmath.headbash.commands.helpers.ServerCommandScheduler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.*;

import static net.blockmath.headbash.commands.helpers.CommandHelpers.perms;


public class BashCommand {
    public static int requiredPermissionLevel = Commands.LEVEL_OWNERS;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("nop")
                        .executes(context -> Command.SINGLE_SUCCESS)
        );

        LiteralCommandNode<CommandSourceStack> literalCommandNode = dispatcher.register(
                Commands.literal("bash")
                        .requires(perms(requiredPermissionLevel))
        );

        dispatcher.register(
                Commands.literal("bash")
                        .requires(perms(requiredPermissionLevel))
                        .then(
                                Commands.literal("run").then(
                                        Commands.argument("cmd", StringArgumentType.greedyString())
                                                .executes(context -> {
                                                    __exec(context.getSource(), StringArgumentType.getString(context, "cmd"));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                )
                        )
                        .then(
                                Commands.literal("exec").then(
                                        Commands.argument("cmd", StringArgumentType.string())
                                                .fork(literalCommandNode, context -> bash_exec(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "cmd")
                                                ))
                                )
                        )
                        .then(
                                Commands.literal("echo").then(
                                        Commands.argument("msg", StringArgumentType.string())
                                                .fork(literalCommandNode, context -> bash_echo(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "msg")
                                                ))
                                ).then(
                                        Commands.literal("var").then(
                                                Commands.argument("msg", WeirdgeDoubleArgumentType.doubleArg())
                                                        .fork(literalCommandNode, context -> bash_echo(
                                                                context.getSource(),
                                                                "" + WeirdgeDoubleArgumentType.getDouble(context, "msg")
                                                        ))
                                        )
                                )
                        )
                        .then(
                                Commands.literal("for").then(
                                        Commands.argument("var", StringArgumentType.string()).then(
                                                Commands.literal("in").then(
                                                        Commands.literal("range").then(
                                                                Commands.argument("start", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                        Commands.argument("stop", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("step", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_for(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "start"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "stop"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "step"),
                                                                                                context.getInput()
                                                                                        ))
                                                                                )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                        .then(
                                Commands.literal("if").then(
                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg())
                                                .then(
                                                        Commands.literal("eq").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_EQ,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("ne").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_NE,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("gt").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_GT,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("ge").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_GE,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("lt").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_LT,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("le").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_LE,
                                                                                true
                                                                        ))
                                                        )
                                                )
                                )
                        )
                        .then(
                                Commands.literal("unless").then(
                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg())
                                                .then(
                                                        Commands.literal("eq").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_EQ,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("ne").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_NE,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("gt").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_GT,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("ge").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_GE,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("lt").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_LT,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                                .then(
                                                        Commands.literal("le").then(
                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_if(
                                                                                context.getSource(),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_LE,
                                                                                false
                                                                        ))
                                                        )
                                                )
                                )
                        )
                        .then(
                                Commands.literal("let").then(
                                        Commands.argument("var", StringArgumentType.string()).then(
                                                Commands.literal("eq")
                                                        .then(
                                                                Commands.literal("add").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_ADD,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("sub").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_SUB,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("mul").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_MUL,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("div").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_DIV,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("pow").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_POW,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("log").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).then(
                                                                                Commands.argument("val_b", WeirdgeDoubleArgumentType.doubleArg())
                                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                                context.getSource(),
                                                                                                StringArgumentType.getString(context, "var"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                                OPERATOR_LOG,
                                                                                                context.getInput()
                                                                                        ))
                                                                        )
                                                                )
                                                        )
                                                        .then(
                                                                Commands.literal("int").then(
                                                                        Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg()).fork(literalCommandNode, context -> bash_let(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(context, "var"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_b"),
                                                                                OPERATOR_INT,
                                                                                context.getInput()
                                                                        ))
                                                                )
                                                        )
                                                        .then(
                                                                Commands.argument("val_a", WeirdgeDoubleArgumentType.doubleArg())
                                                                        .fork(literalCommandNode, context -> bash_let(
                                                                                context.getSource(),
                                                                                StringArgumentType.getString(context, "var"),
                                                                                WeirdgeDoubleArgumentType.getDouble(context, "val_a"),
                                                                                Double.NaN,
                                                                                OPERATOR_SET,
                                                                                context.getInput()
                                                                        ))
                                                        )
                                        )
                                )
                        )
                        .then(
                                Commands.literal("delay")
                                        .then(
                                                Commands.argument("duration", WeirdgeDoubleArgumentType.doubleArg())
                                                        .fork(literalCommandNode, context -> bash_delay(
                                                                context.getSource(),
                                                                (int)WeirdgeDoubleArgumentType.getDouble(context, "duration"),
                                                                context.getInput(),
                                                                "delay",
                                                                2
                                                        ))
                                        )
                        )
        );
    }

    public static final int OPERATOR_ADD = 1;
    public static final int OPERATOR_SUB = 2;
    public static final int OPERATOR_MUL = 3;
    public static final int OPERATOR_DIV = 4;

    public static final int OPERATOR_POW = 5;
    public static final int OPERATOR_LOG = 6;

    public static final int OPERATOR_SET = 7;

    public static final int OPERATOR_INT = 8;

    public static final int OPERATOR_EQ = 16;
    public static final int OPERATOR_NE = 17;
    public static final int OPERATOR_GT = 18;
    public static final int OPERATOR_GE = 19;
    public static final int OPERATOR_LT = 20;
    public static final int OPERATOR_LE = 21;

    private static void __exec(CommandSourceStack source, String command, int delay) {
        if (delay == 0) {
            __exec(source, command);
        } else {
            ServerCommandScheduler.get(source.getServer()).schedule(() -> __exec(source, command), delay);
        }
    }

    public static void __exec(CommandSourceStack source, String command) {
        source.getServer().getCommands().performCommand(
                source.getServer().getCommands().getDispatcher().parse(
                        command,
                        source
                ),
                command //.split(" ")[0]
        );
    }

    private static void __var_set_impl(CommandSourceStack source, String var, double val, String command, String note, int clen, boolean isInt) {
        String[] newCommand = command.split(" ");
        int cmd_st = -1;
        for (int i = 0; i < newCommand.length; ++i) {
            if (newCommand[i].equals(note)) {
                cmd_st = i;
                break;
            }
        }
        newCommand = Arrays.copyOfRange(newCommand, cmd_st + clen, newCommand.length);
        for (int i = 0; i < newCommand.length; ++i) {
            if (newCommand[i].equals("$" + var)) {
                if (isInt) {
                    newCommand[i] = Integer.toString((int) val);
                } else {
                    newCommand[i] = Double.toString(val);
                }
            }
        }
        String cmd_buf = "bash " + String.join(" ", newCommand);

        __exec(source, cmd_buf);
    }


    public static List<CommandSourceStack> bash_delay(CommandSourceStack source, int delay, String command, String note, int clen) {
        String[] newCommand = command.split(" ");
        int cmd_st = -1;
        for (int i = 0; i < newCommand.length; ++i) {
            if (newCommand[i].equals(note)) {
                cmd_st = i;
                break;
            }
        }
        newCommand = Arrays.copyOfRange(newCommand, cmd_st + clen, newCommand.length);

        String cmd_buf = "bash " + String.join(" ", newCommand);
        __exec(source, cmd_buf, delay);

        return List.of(new CommandSourceStack[]{});
    }


    private static List<CommandSourceStack> bash_exec(CommandSourceStack source, String cmd) {

        __exec(source, cmd);

        return List.of(new CommandSourceStack[]{source});
    }

    private static List<CommandSourceStack> bash_echo(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), true);

        return List.of(new CommandSourceStack[]{source});
    }

    public static List<CommandSourceStack> bash_if(CommandSourceStack source, double val_a, double val_b, int op, boolean when) {
        boolean result = switch (op) {
            case OPERATOR_EQ -> val_a == val_b;
            case OPERATOR_NE -> val_a != val_b;
            case OPERATOR_GT -> val_a > val_b;
            case OPERATOR_GE -> val_a >= val_b;
            case OPERATOR_LT -> val_a < val_b;
            case OPERATOR_LE -> val_a <= val_b;
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };

        if (result == when) {
            return List.of(new CommandSourceStack[]{source});
        } else {
            return List.of(new CommandSourceStack[]{});
        }
    }

    public static List<CommandSourceStack> bash_let(CommandSourceStack source, String var, double val_a, double val_b, int op, String command) {
        double result = switch (op) {
            case OPERATOR_ADD -> val_a + val_b;
            case OPERATOR_SUB -> val_a - val_b;
            case OPERATOR_MUL -> val_a * val_b;
            case OPERATOR_DIV -> val_a / val_b;
            case OPERATOR_POW -> Math.pow(val_a, val_b);
            case OPERATOR_LOG -> Math.log(val_b) / Math.log(val_a);
            case OPERATOR_SET -> val_a;
            case OPERATOR_INT -> (int)val_a;
            default -> throw new IllegalStateException("Unexpected value: " + op);
        };

        System.out.println(command);

        __var_set_impl(source, var, result, command, "let", op == OPERATOR_SET ? 4 : (op == OPERATOR_INT ? 5 : 6), op == OPERATOR_INT);

        return List.of(new CommandSourceStack[]{});
    }

    public static List<CommandSourceStack> bash_for(CommandSourceStack source, String var, double start, double stop, double step, String command) {
        if (var.equals("_")) {
            ArrayList<CommandSourceStack> list = new ArrayList<>();

            for (int i = (int) start; i <= stop; i += (int) step) {
                list.add(source);
            }

            return list;
        } else {
            for (int i = (int) start; i <= stop; i += (int) step) {
                __var_set_impl(source, var, i, command, "for", 7, true);
            }
            return List.of(new CommandSourceStack[]{});
        }
    }
}
