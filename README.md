
# HeadBash Commands

### (So named because I had to bash my head on my computer to get some of this stuff to work.)

HeadBash Commands is a small mod that adds a few simple command utilities. In addition to basic commands such as
`/sethome`, the main feature of this mod is the `/bash` command, which allows you to do limited in-command scripting
in a format similar to `/execute`. The permission level of each command is configurable, and the `/bash` command
doesn't natively allow players to bypass permission restrictions like `/execute` would.

## `/bash`

Formatted similar to `/execute`, with the following options:

`let <var> eq <value>` - Replace all future occurences of `$<var>` with `<value>`. Only supports numeric types.

`let <var> eq (add|sub|mul|div|pow|log) <val_a> <val_b>` - Same as above, but performs the specified calculation
and assigns the result to `$<var>`.

`let <var> eq int <value>` - Same as above, but converts the number to an integer. Useful for `floor` operations
and pretty-print.

`for <var> in range <start> <stop> <step>` - Forks the command into multiple instances with `let <var> eq <value>`
for each step in the for-loop. Ordering is not guaranteed, but is generally consistent.

`(if|unless) <val_a> (eq|ne|gt|ge|lt|le) <val_b>` - The command is continued only if/unless the specified
comparison condition is true. Otherwise, the branch stops.

`delay <duration>` - Suspends execution of the current branch for `<duration>` ticks.

`run <cmd>` - Runs another command. Works similarly to `/execute`'s `run` option, but doesn't support autocomplete
to allow for lazy numeric parsing (waiting for previous options to be processed before parsing the command),
rather than having the command fail with errors like "`$i` is not a double".