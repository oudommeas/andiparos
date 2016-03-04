# Howto build Andiparos from sources #

## Building Andiparos ##
Checkout the sources using the subversion client of your choice. Then, startup Eclipse and import the Andiparos project. (Sure, you could use and other Java IDE as well.)

In the Package Explorer of Eclipse, go to the directory _build_, right click on the file _build-not-packed.xml_ and select _Run As --> 1 Ant Build_.

When the ant building process succeeded, a new directory structure has been created, that can be found in the directory _build/build_.

## Running Andiparos inside Eclipse ##
When running Andiparos, use **org.parosproxy.paros.Andiparos** as the main class.

Make sure that when you try to run Andiparos in your IDE the 'output' directory has to be set as the working directory.

In Eclipse you configure this via the "Run Configurations" dialog - select the "Arguments" tab and change the "Working Directory" to:

`${workspace_loc:Andiparos/output}`