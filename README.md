<h1>Island Reservation Service</h1>

Hello! This is the `island-rsv-srv` which can be used to reserve camping spots on the luxurious Upgradikii 
island.

To run this service locally, I suggest you have docker installed and running, as it will avoid you have to setting up a database.
You must also have maven installed.

<h3>To run locally</h3>
From the project root, run 

```mvn package```

followed by a 

```docker build ./ -t island-rsv-srv```

and finally 

```docker-compose up```

All db migrations will run and the web server should be up and running at `localhost:8080` ! Happy vacation!
