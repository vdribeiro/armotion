
#ifndef MAIN_HEADER_H
#define MAIN_HEADER_H

/*
Includes
*/
#include <stdlib.h>
#include <stdio.h>
#include <winsock2.h>
#include <string.h>
#include <math.h>
#include <malloc.h>
#include <time.h>
#include <vector>
#include <iostream>

#include <GL/glui.h>
#include <GL/glut.h>
#include <GL/GLU.h>
#include <GL/GL.h>

#ifdef _WIN32
#include <windows.h>
#include <windowsx.h>
#else
#include <strings.h>
#endif

#include <AR/gsub.h>
#include <AR/matrix.h>
#include <AR/video.h>
#include <AR/param.h>
#include <AR/ar.h>
#pragma comment(lib, "wsock32.lib")

/* set up the video format globals */

#ifdef _WIN32
char    *vconf = "Data\\WDM_camera_flipV.xml";
#else
char    *vconf = "";
#endif

int             xsize;
int             ysize;
int             thresh = 100;
ARParam         cparam;

int             mouse_ox;
int             mouse_oy;
int             mouse_st = 0;
double          a =   0.0;
double          b = -45.0;
double          r = 500.0;

int             target_id;
int             target_id2;
double          target_center[2] = {0.0, 0.0};
double          target_width = 80.0;

//using namespace std;

/*
Defines
*/
//#define TRUE  = 1;
//#define FALSE = 0;

/*
Sockets
*/
char *server_name= "localhost";
int port = 2012;
struct sockaddr_in server;
struct hostent *hp;
int addr = 0;
WSADATA wsaData;
SOCKET  conn_socket;
int socket_type = SOCK_DGRAM; //SOCK_STREAM;
int retval = 0;

/*
GL
*/
GLUI  *glui;
GLUquadric* glQ;
float xy_aspect;
//variaveis para criacao de botao de rotacao e de zoom
GLUI_Rotation *view_rot;
//GLUI_Translation * trans_z;
// matriz de transf. geometrica utilizada pelo botao esferico
float view_rotate[16] = { 1,0,0,0,
                          0,1,0,0,
                          0,0,1,0,
                          0,0,0,1 };

float iden_matrix[16] = { 1,0,0,0,
                          0,1,0,0,
                          0,0,1,0,
                          0,0,0,1 };

// vector de posicao utilizado pelo botao de afastamento
//float obj_pos[] = { 0.0, 0.0, 0.0 };

// matrix
double quat[4], pos[3];
double  fixed_trans[3][4];
double  target_trans[3][4];
double  cam_trans[3][4];

// gl menu flag
bool glon = true;

// string
char string[256];

int atype = 0;
int inst1 = 1;
int inst2 = 10;
int length = 16;
int beat = 0;

/* function definitions */

/* Socket */
static bool connect(void);
static bool sendmsg(char *msg);
static void close(void);

/* AR */
static int init(void);
static void cleanup(void);
static void keyEvent( unsigned char key, int x, int y);
static void mouseEvent(int button, int state, int x, int y);
static void motionEvent( int x, int y );
static void mainLoop(void);
static void getResult( ARMarkerInfo *marker_info , int player);

static void callback(int id);
static void myGlutReshape(int w, int h);
static void myGlutIdle( void );

/* GL */
static void setup_light( void );

// debug
static void print_string( char *string , int player );
static int  draw_exview( double a, double b, double r, double trans[3][4], int xwin, int ywin );
static void get_trans( double a, double b, double r, double trans[3][4] );
static void draw_axis( void );
static void draw_camera( double trans[3][4] );

// graphic math
static void newellSquare(GLdouble *vec1,GLdouble *vec2,GLdouble *vec3,GLdouble *vec4,GLdouble *normal);
static void normalise(GLdouble *vec);

// shapes
static void drawBox(GLdouble dimx, GLdouble dimy, GLdouble dimz, float s_factor, float t_factor);
static void drawClosedCylinder(GLUquadric* glQ,	GLdouble base, GLdouble top, GLdouble height, GLint slices, GLint stacks, bool drawbase, bool drawtop);

// draw
static int  draw( char name[], double trans[3][4], int xwin, int ywin , int player);
static int  draw_object( char *name, double gl_para[16], int xwin, int ywin , int player);

#endif
