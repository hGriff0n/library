package library

import (
	"os"
	"sort"

	"github.com/urfave/cli"	// https://github.com/urfave/cli

	// "github.com/jinzhu/gorm"
    // _ "github.com/jinzhu/gorm/dialects/sqlite"
)

// A simple command line program to record information about my home library

// Might convert this over to another language
// The sql driver doesn't work on windows ??

type Book struct {
	gorm.Model
	// ISBN `gorm:"primary_key"`
	Title string
	Pages uint
	Read uint
}

func main() {
	// Setup and connect to the database
	// db, err := gorm.Open("sqlite3", "C:/Users/ghoop/Desktop/library/library.db")
	// if err != nil {
	// 	panic("Failed to connect to database")
	// }
	// defer db.Close()

	// db.AutoMigrate(&Book{})


	app := cli.NewApp()

	app.Commands = []cli.Command{
		{
			Name:    "add",
			Aliases: []string{"a"},
			Usage:   "add a new book to the list",
			Action:  func(c *cli.Context) error {
				// file.Append(c.Args()[0] + "0 0")
				db.Create(&Book{Title: c.Args()[0], Pages: 0, Read: 0})
				return nil
			},
		},
		{
			Name:    "update",
			Aliases: []string{"u"},
			Usage:   "update the information for a given book",
			Action:  func(c *cli.Context) error {
				// var book Book
				// db.First(&book, "title = ?", "")
				// TODO:
				return nil
			},
		},
		{
			Name:    "remove",
			Aliases: []string{"u"},
			Usage:   "update the information for a given book",
			Action:  func(c *cli.Context) error {
				// var book Book
				// db.First(&book, "title = ?", "")
				// db.Delete(&book)
				return nil
			},
		},
		{
			Name:    "show",
			Aliases: []string{"s"},
			Usage:   "display the information",
			Action:  func(c *cli.Context) error {
				// var books []Book
				// db.Find(&books)

				return nil
			},
		}
	}

	// What is this doing ???
	// sort.Sort(cli.FlagsByName(app.Flags))
	// sort.Sort(cli.CommandsByName(app.Commands))

	app.Run(os.Args)
}