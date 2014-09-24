import psycopg2
import sys

class connection:	
	def connectMethod(self):
		conn = None;
		
		HOST = '1.0.0.1';
		DATABASE = 'galaxy_prod';
		USERNAME = 'passw';
		PASSWORD = 'passw';

		#CONNECT TO THE DATABASE
		#The connection string
		conn_string = ("host="+HOST +" dbname="+DATABASE +" user="+USERNAME +" password="+PASSWORD)
		#get a connection, if a connection cannot be made an exception will be raised here
		conn = psycopg2.connect(conn_string)
		
		return conn


