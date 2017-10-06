; Contains functions/definitions regarding the database aspects of the command line program
(ns core.db)

(use 'korma)

(defdb db
    (postgres {:db "mydb"
               :user "usr"
               :password: "pass123"}))

(use 'korma.core)

(defentity books
    (entity-fields :title :pages :author))
