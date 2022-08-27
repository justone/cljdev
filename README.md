# cljdev

A small amount of bootstrap to make starting a Clojure development environment
easier.

This repo facilitates starting a new Clojure project with almost no set up and
then supports growing into a more capable of tools.

# Install

Add this alias to your `deps.edn` file (either at project level or in `~/.clojure/deps.edn`):

```
:repl {:extra-deps {io.github.justone/cljdev {:sha "b1956efcbafb5e1b5e0fc9bfb5715c25e9efae5e"}}
       :main-opts  ["-m" "cljdev.repl"]
       :exec-fn cljdev.repl/start}
```

# Usage

Start with `-M` for default behavior. This will start an nREPL and pREPL server.

```
clojure -M:repl
```

## Customizing with `-X`

Note: this requires using version [`1.10.1.697`](https://clojure.org/releases/tools#v1.10.1.697) or later.

Here are the options and their default values:

| Option      | Description                      | Default |
|-------------|----------------------------------|---------|
| `:nrepl`    | Start nREPL server?              | true    |
| `:prepl`    | Start pREPL server?              | true    |
| `:start-ns` | Which namespace to start in?     | 'dev    |
| `:refresh`  | Call c.t.n.r/refresh at startup? | false   |

To override on the command line, add extra key/value pairs:

```
clojure -X:repl :prepl false :refresh true
```

Or, to override for the project, add an `:exec-args` key to the alias:

```
:exec-args {:prepl false
            :refresh true}
```
