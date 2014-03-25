/* example 6c.c */
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#define BUFSIZ 1024
#define MSG1 "I got your request"
#define MSG2 "Yes, sir"

main(argc,argv)
int argc;
char *argv[];
{ 
char buf[BUFSIZ];
struct sockaddr_in remote;
int sk;
int i;
struct hostent *hp,*gethostbyname();

sk=socket(AF_INET,SOCK_STREAM,0);

remote.sin_family=AF_INET;
hp=gethostbyname(argv[1]);
bcopy(hp->h_addr,(char*)&remote.sin_addr,hp->h_length);
remote.sin_port=atoi(argv[2]);

connect(sk,(struct sockaddr *)&remote,sizeof(remote));

/*Read input from stdin an send it to the server*/
for (i = 0; i < BUFSIZ; i++)
	buf[i] = '\0';
printf("Type the first message to be echoed\n");
while(read(0,buf,BUFSIZ)>1)
 {
  write(sk,buf,strlen(buf)+1);
  for (i = 0; i < BUFSIZ; i++)
	buf[i] = '\0';
  printf("Type the next message to be echoed\n");
 }
close(sk);
}
