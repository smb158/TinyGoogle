/*************************************************************************/
/* This example shows how to establish a datagram based communication    */
/* in Internet domain. The programs are contained in example ex2a.c and  */
/* ex2b.c.                                                               */
/* To compile:                                                           */
/*    cc -o ex2b ex2b.c                                                  */
/*         or                                                            */
/*    gcc -o ex2b ex2b.c -lsocket -lnsl                                  */
/*    (-lsocket and -lnsl may not be required on all machines)           */
/* To execute the program :                                              */
/*    at the local machine type ex2a (or ./ex2a) and get the port number */
/*    at the remote machine type ex2b (or ./ex2b) with host address,     */ 
/*    such as unixs2.cis.pitt.edu, and port number.                      */
/*                                                                       */
/* local> ex2a (or ./ex2a)                                               */
/*       socket has port 1664                                            */
/*                    remote> ex2b (or .ex2b) machine.cs.pitt.edu  1664  */
/*                                                                       */
/*************************************************************************/

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <stdio.h>



#define MSG "Yes, I am !!!"

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

remote.sin_port=htons(atoi(argv[2]));

/*********************************************************/
/* Send the message                                      */ 
/*********************************************************/

sendto(sk,MSG,strlen(MSG)+1,0,(struct sockaddr *)&remote,sizeof(remote));
printf("Success 1\n");

/*********************************************************/
/* Read what the other machine sent                      */ 
/*********************************************************/
read(sk,buf,BUFSIZ);
printf("Success 2\n");
printf("%s\n",buf);
close(sk);
}
