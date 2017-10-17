package main

import (
	"os"
	"encoding/json"
	"fmt"
	"strconv"

	"github.com/urfave/cli"	// https://github.com/urfave/cli
	"github.com/boltdb/bolt" // https://github.com/boltdb/bolt
)

// A simple command line program to record information about my home library

type Book struct {
	Title string
	Pages uint64
	Read uint64
}

func main() {
	// Setup and connect to the database
	db, err := bolt.Open("books.db", 0600, nil)
	if err != nil {
		// log.Fatal(err)
	}
	db.Update(func(tx *bolt.Tx) error {
		// Create the initial bucket
		_, err := tx.CreateBucketIfNotExists([]byte("books"))
		return err
	})
	defer db.Close()


	app := cli.NewApp()

	// TODO: Extend this with more capabilities (better command line args)
	// TODO: Figure out a better way of organizing everything (better schema)
	app.Commands = []cli.Command{
		{
			Name:    "add",
			Aliases: []string{"a"},
			Usage:   "add a new book to the list",
			Action:  func(c *cli.Context) error {
				return addCommand(c, db)
			},
		},
		{
			Name:    "update",
			Aliases: []string{"u"},
			Usage:   "update the information for a given book",
			Action:  func(c *cli.Context) error {
				return updateCommand(c, db)
			},
		},
		{
			Name:    "remove",
			Aliases: []string{"r"},
			Usage:   "update the information for a given book",
			Action:  func(c *cli.Context) error {
				return removeCommand(c, db)
			},
		},
		{
			Name:    "show",
			Aliases: []string{"s"},
			Usage:   "display the information",
			Subcommands: []cli.Command{
				{
					Name: "all",
					Usage: "display everything",
					Action: func(c *cli.Context) error {
						return showAllCommand(c, db)
					},
				},
				{
					Name: "where",
					Usage: "filter results according to some argument",
					Action: func(c *cli.Context) error {
						fmt.Printf("Flag: %v\n", c.FlagNames())
						return showAllCommand(c, db)
					},
					Flags: []cli.Flag{
						cli.IntFlag{
							Name: "pages, p",
							Value: 0,
							Usage: "Number of pages to filter on",
						},
					},
				},
			},
		},
	}

	app.Run(os.Args)
}


func addCommand(c *cli.Context, db *bolt.DB) error {
	return db.Update(func(tx *bolt.Tx) error {
		bucket := tx.Bucket([]byte("books"))
		pages, err := strconv.ParseUint(c.Args()[2], 0, 64)
		if err != nil {
			return err
		}

		read, err := strconv.ParseUint(c.Args()[1], 0, 64)
		if err != nil {
			return err
		}

		encoded, err := json.Marshal(Book{ 
			Title: c.Args()[0],
			Pages: pages,
			Read: read,
		})
		if err != nil {
			return err
		}

		return bucket.Put([]byte(c.Args()[0]), encoded)
	})
}

func updateCommand(c *cli.Context, db *bolt.DB) error {
	return db.Update(func(tx *bolt.Tx) error {
		bucket := tx.Bucket([]byte("books"))
		v := bucket.Get([]byte(c.Args()[0]))

		var book Book
		if err := json.Unmarshal(v, &book); err != nil {
			return err
		}
		
		read, err := strconv.ParseUint(c.Args()[1], 0, 64)
		if err != nil {
			return err
		}

		book.Read = read

		encoded, err := json.Marshal(book)
		if err != nil {
			return err
		}

		return bucket.Put([]byte(c.Args()[0]), encoded)
	})
}

func removeCommand(c *cli.Context, db *bolt.DB) error {
	return db.Update(func(tx *bolt.Tx) error {
		bucket := tx.Bucket([]byte("books"))
		return bucket.Delete([]byte(c.Args()[0]))
	})
}

func showAllCommand(c *cli.Context, db *bolt.DB) error {
	return db.View(func(tx *bolt.Tx) error {
		bucket := tx.Bucket([]byte("books"))
		cur := bucket.Cursor()

		var totalPages uint64 = 0
		var readPages uint64 = 0
		for k,v := cur.First(); k != nil; k,v = cur.Next() {
			var book Book
			if err := json.Unmarshal(v, &book); err != nil {
				return err
			}

			fmt.Printf("Title: %v - Total: %v - Read: %v\n", string(k), book.Pages, book.Read)
			totalPages += book.Pages
			readPages += book.Read
		}
		
		fmt.Printf("\nRead Pages: %v\nTotal Pages: %v\n", readPages, totalPages)

		return nil
	})
}