# JVA Bookstore

A Java web application starter using Servlet, JSP/JSTL, Bootstrap, JDBC, PostgreSQL, Sitemesh (Decorator), and JWT authentication. Ready for deployment on Heroku.

## Features
- Servlet + JSP/JSTL
- Bootstrap for UI
- JDBC for PostgreSQL
- Sitemesh for layout/decorator
- JWT for authentication
- Heroku deployment ready

## Quick Start
1. Configure your PostgreSQL credentials in `src/main/resources/db.properties`.
2. Build with Maven:
   ```sh
   mvn clean package
   ```
3. Deploy to Heroku (ensure you have the Heroku CLI and a PostgreSQL add-on):
   ```sh
   heroku create
   heroku addons:create heroku-postgresql:hobby-dev
   git push heroku main
   ```

## Example Structure
- `src/main/java/` - Java source code (Servlets, utils)
- `src/main/webapp/` - JSPs, assets, WEB-INF
- `src/main/resources/` - Properties files

## Credits
Inspired by torder.net