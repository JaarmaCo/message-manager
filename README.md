# Message Manager

This is the start of a small backend for a Spring based messaging app.

The project is intended to be completable within one or a few weeks, so
the project scope has been made deliberately narrow.

## PLANNED FEATURES

These are the planned features that will be implemented in this project:

1. Password protected user accounts.
2. Person to person communication via a centralized server.
3. Message persistance via a localized PostgreSQL database.

If there is time for it:

1. Chat rooms.
2. Rich media messages.
3. Live conferences.

## REPOSITORY STRUCTURE

Because Java programming (especially for Spring framework applications)
involves a lot of filesystem sprawl, a proper file structure is an absolute
must for maintaining a comprehensible codebase.

The file structure that will be used in this project is a full stack modular
approach. That is, instead of having:

- `/app/controller/MessageController.java`
- `/app/controller/AuthController.java`
- `/app/service/MessageService.java`
- `/app/service/AuthService.java`

For every layer, the file structure is divided on the complete application
module:

- `/app/message/MessageController.java`
- `/app/message/MessageService.java`
- `/app/auth/AuthController.java`
- `/app/auth/AuthService.java`

This structure is preferred because it keeps components that are tightly
coupled close together in the file structure.

