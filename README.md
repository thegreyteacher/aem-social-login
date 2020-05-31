# AEM Social Login (Google OAuth 2)

[Detailed Doc](https://dev.to/thegreyteacher/aem-social-login-google-oauth2-23pf)

## Testing OAuth flow
1. Build using *mvn clean install*
2. In case AEM instance is running on 4502 then use the profile *autoInstallPackage* otherwise mention the host and 
port explicitly or deploy the package manually to crx package manager.
3. Configure App client id and secret in *com.adobe.granite.auth.oauth.provider-tgt-google* configuration on 
OSGi config manager.
4. Hit URL: {your-domain}/j_security_check?configid=tgt-google

Note: The newly created user doesn't have any permission, so on author instance your will get 404 on successful login.

# Sample AEM project template

This is a project template for AEM-based applications. It is intended as a best-practice set of examples as well as a potential starting point to develop your own functionality.

## Modules

The main parts of the template are:

* core: Java bundle containing all core functionality like OSGi services, listeners or schedulers, as well as component-related Java code such as servlets or request filters.
* ui.apps: contains the /apps (and /etc) parts of the project, ie JS&CSS clientlibs, components, templates, runmode specific configs as well as Hobbes-tests

## How to build

To build all the modules run in the project root directory the following command with Maven 3:

    mvn clean install

If you have a running AEM instance you can build and package the whole project and deploy into AEM with

    mvn clean install -PautoInstallPackage

Or to deploy it to a publish instance, run

    mvn clean install -PautoInstallPackagePublish

Or alternatively

    mvn clean install -PautoInstallPackage -Daem.port=4503

Or to deploy only the bundle to the author, run

    mvn clean install -PautoInstallBundle

## Maven settings

The project comes with the auto-public repository configured. To setup the repository in your Maven settings, refer to:

    http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html
