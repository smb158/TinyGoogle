/*************************************************************************/
/* This program implements the service provided by the server.           */
/* It is a child of the server and receives message from the stdin.      */
/* To execute this program read the instructions in ex6a.c.              */
/*************************************************************************/

#include <stdio.h>

main()
{ 
char buf[BUFSIZ];

while(read(0,buf,BUFSIZ)>0)
 {
  printf("%s",buf);
 }
 printf("****The client ended connection****\n");
}
