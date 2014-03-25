/*************************************************************************/
/* This example shows how to establish a datagram based communication    */
/* in Internet domain. The programs are contained in example ex5a.c and  */
/* ex5b.c.                                                               */
/* To compile:                                                           */
/*    cc -o ex5a ex5a.c                                                  */
/*          or                                                           */
/*    gcc -o ex5a ex5a.c -lsocket                                        */
/*    (-lsocket may not be required on all machines)                     */
/* To execute the program :                                              */ 
/*    at the local machine (such as icarus.lis.pitt.edu or               */
/*    unixs3.cis.pitt.edu) type ex5a (or ./ex5a) and get the port        */ 
/*    number                                                             */
/*    at the remote machine (such as hibiscus) type ex5b (or ./ex5b)     */
/*    with host address and port number.                                 */    
/*                                                                       */
/* icarus> ex5a                                                          */
/*       socket has port 1664                                            *     
/*                             hisbiscus> ex5b icarus.lis.pitt.edu 1664  */
/*************************************************************************/

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <string.h>
#include <stdio.h>

#define MSG "What do you think about the future Cloud Computing?"

main()
{
struct	sockaddr_in local,remote;
int	sk,rlen=sizeof(remote),len=sizeof(local);
char	buf[BUFSIZ];

sk=socket(AF_INET,SOCK_DGRAM,0);

local.sin_family=AF_INET;         /* Define the socket domain   */
local.sin_addr.s_addr=INADDR_ANY; /* Wild-card, machine address */
local.sin_port=0;                 /* Let the system assign the port number */

bind(sk,(struct sockaddr *)&local,sizeof(local));

getsockname(sk,(struct sockaddr *)&local,&len);          /* Get the port
number assigned */
printf("socket has port %d\n",local.sin_port); /* Display port number */

bzero(buf, sizeof(buf));
recvfrom(sk,buf,BUFSIZ,0,(struct sockaddr *)&remote,&rlen);
printf("%s\n",buf);
sendto(sk,MSG,strlen(MSG)+1,0,(struct sockaddr *)&remote,sizeof(remote));
close(sk);
}
