/*************************************************************************/
/* This example shows how to establish a datagram based communication    */
/* in Internet domain. The programs are contained in example ex5a.c and  */
/* ex5b.c.                                                               */
/* To compile:                                                           */
/*    cc -o ex5b ex5b.c                                                  */
/*        or                                                             */
/*    gcc -o ex5b ex5b.c -lsocket -lnsl                                  */
/*    (-lsocket and -lnsl may not be required on all machines)           */
/* To execute the program :                                              */
/*    at the local machine (local) type ex5a (or ./ex5a) and get the     */ 
/*    port number                                                        */
/*    at the remote machine (remote) type ex5b (or ./ex5b) with host     */ 
/*    address, such as unixs3.cis.pitt.edu, and port number.             */
/*                                                                       */
/* local> ex5a                                                           */
/*       socket has port 1664                                            */
/*                             remote> ex5b remote.cs.pitt.edu 1664      */
/*************************************************************************/


#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>

#define MSG "There are simply GREAT!!!"

main(argc,argv)
int argc;
char *argv[];
{

int	sk;
char	buf[BUFSIZ];
struct	sockaddr_in remote;
struct	hostent *hp,*gethostname();

sk=socket(AF_INET,SOCK_DGRAM,0);

remote.sin_family=AF_INET;

/*********************************************************/
/* Get the remote machine address from its symbolic name */
/* given by the first argument of the command line       */
/*********************************************************/

hp=gethostbyname(argv[1]);
bcopy(hp->h_addr,&remote.sin_addr.s_addr,hp->h_length);

/*********************************************************/
/* Get remote port number given by the second argument   */ 
/* of the command line                                   */
/*********************************************************/

remote.sin_port=atoi(argv[2]);

/*********************************************************/
/* Send the message                                      */ 
/*********************************************************/

sendto(sk,MSG,strlen(MSG)+1,0,(struct sockaddr *)&remote,sizeof(remote));
bzero(buf, sizeof(buf));
read(sk,buf,BUFSIZ);
printf("%s\n",buf);
close(sk);
}
