/*************************************************************************/
/* This example shows how to establish a datagram based communication    */
/* in Internet domain. The programs are contained in example ex2a.c and  */
/* ex2b.c.                                                               */
/* To compile:                                                           */
/*    cc -o ex2a ex2a.c                                                  */
/*         or                                                            */
/*    gcc -o ex2a ex2a.c -lsocket                                        */
/*     (-lsocket may not be required on all machines)                    */
/* To execute the program :                                              */ 
/*    at the local machine type ex2a (or ./ex2a) and get the port number */
/*    at the remote machine type ex2b (or ./ex2b) with host address,     */
/*    such as unixs2.cis.pitt.edu, and port number.                      */
/*                                                                       */
/* local> ex2a (or ./ex2a)                                               */
/*       socket has port 1664                                            */    
/*                    remote> ex2b (or ./ex2b) machine.cs.pitt.edu 1664  */ 
/*                                                                       */
/*  To test time out, execute ex2a at the local site without executing   */
/*  ex2b at the remote site.                                             */
/*                                                                       */
/*************************************************************************/

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <sys/ioctl.h>
#include <string.h>
#include <stdio.h>
#include <sys/time.h>
#include <signal.h>
#include <unistd.h>


/* Message to be send by the local machine */

#define MSG "Are you sure this works? I doubt it!!!"
#define TIMER  10  /*Timeout value*/ 
#define CONTINUE     1 



main()
{
struct	sockaddr_in local,remote;
int	sk,rlen=sizeof(remote),len=sizeof(local);
int     status;
char	buf[BUFSIZ];


/*Data structure to handle timeout*/

struct timeval before;
struct timeval timer;
struct timeval *tvptr;
struct timezone tzp;

/* Data structure for the select I/O */

fd_set ready_set;
fd_set test_set;
int maxfd;
int nready;
int nbytes;


sk=socket(AF_INET,SOCK_DGRAM,0);


/*Set up the I/O for the socket, nonblocking*/
  maxfd = sk;
  FD_ZERO(&ready_set);
  FD_ZERO(&test_set);
  FD_SET(sk, &test_set);

  timer.tv_sec = TIMER;
  timer.tv_usec = 0;
  tvptr = &timer;


local.sin_family=AF_INET;         /* Define the socket domain   */
local.sin_addr.s_addr=INADDR_ANY; /* Wild-card, machine address */
local.sin_port=0;                 /* Let the system assign the port number */

bind(sk,(struct sockaddr *)&local,sizeof(local));

getsockname(sk,(struct sockaddr *)&local,&len);  /* Get the port number 
assigned*/
printf("socket has port %d\n",ntohs(local.sin_port)); /* Display port number */

/* Set up the time out by getting the time of the day from the system */
  gettimeofday(&before, &tzp);
  status=CONTINUE;
  while (status==CONTINUE)
   {

      memcpy(&ready_set, &test_set, sizeof(test_set));
      nready = select(maxfd+1, &ready_set, NULL, NULL, tvptr);
       {
                switch(nready)
                  {

                 case -1:

                        perror("\nSELECT: unexpected error occured " );
                        exit(-1);
                        status=-1;
                        break;

                  case 0:

                        /* timeout occuired */
			printf("\nTIMEOUT...");
                        status=-1;
			break;

		 default:

                        if (FD_ISSET(sk, &ready_set))
                          {
/* Receive from the remote side                                        */

recvfrom(sk,buf,BUFSIZ,0,(struct sockaddr *)&remote,&rlen);
                            printf("%s\n",buf);

/* Send to the remote side                                             */

sendto(sk,MSG,strlen(MSG)+1,0,(struct sockaddr *)&remote,sizeof(remote));
                            close(sk);
                            status=-1;
                           }
		}

       }
    }

}
