# Introducing the BeanShell in Andiparos #

## Introduction ##

The [BeanShell](http://www.beanshell.org) is an interactive Java shell that can be used to execute BeanShell scripts. These scripts are a simplified form of Java that use many elements from Java syntax, but in a simpler scripting format. All Java code is also valid BeanShell code.

BeanShell integration in Andiparos enables you to write scripts using the Andiparos functions and data set. This can be a very powerful feature for analyzing web applications.


## BeanShell Console ##

The console is started from the Tools menu, and contains a split screen where the top half is the interactive BeanShell console and the bottom half is a simple text editor. For complex scripts, you're encouraged to use a Java editor. Scripts can be loaded, saved and evaluated from the editor.
When the BeanShell starts a number of objects from Andiparos are available for use, namely:
  * the Model singleton, through the object named **model**
  * the SiteTree tree of current sites through **sites**
  * an instance of HttpSender through the **sender** object

Let's start with a simple example of creating and sending an HTTP request directly in the interactive console:

## Simple HTTP Request ##

The first thing you should notice about the BeanShell is that it's loosely typed. It is not necessary to declare variables before using them – this makes scripts a bit more concise than regular Java. But of course, if you want to define the type you can. To fully utilize the power of the BeanShell, you should familiarize yourself with Andiparos' internals. The sender object is the same instance as is used by the Manual Request Editor and will therefore automatically use proxy settings set in the Andiparos configuration.

```
TODO: HTTP Post example
```


## Using the SiteMap ##

For the next example, we'll do something a little more useful and typical: Iterate through all the site nodes and check for the existence of a file. A script has already been created that accomplishes this, choose Load and select the tree.bsh file (from the scripts directory). Before clicking Evaluate, first browse to a site through Andiparos to populate the SiteTree on the left side.

Now click on evaluate to execute the script that's in the editor. If there are no errors, then you can now start using the object defined in the script by issuing these commands:
```
t = Tree();
```

Which constructs a new Tree object and assigns a reference to t.
```
t.find(sites.getRoot(), "index.html");
```

Call the find method on t, which takes a SiteNode as the first argument and a resource to find as the second. In this case, the method will iterate through all the nodes in the tree, because we started at the root, and will find index.html files.

Instead of iterating through all the nodes, we could choose to start a specific node by using the findChild method e.g.:

This should give you some idea of the power of the BeanShell in Andiparos. But to fully exploit it, you'll need to familiarise yourself with the internal API and the BeanShell's features. The BeanShell has been setup so as to allow full access to all internal objects and methods – even private ones.


## Tips ##

Use the unset(String) command to unset any declared variables, methods or objects. This is useful if you want to replace a method declaration in the current namespace. Note that the command takes a String argument, not an Object, so to unset the t object we used above, it should be: unset("t"); and not unset(t);