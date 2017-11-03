
#include "main_header.h"

/*
MAIN
*/

int main(int argc, char **argv)
{
	//Sockets
	connect();

	//GL
	glutInit(&argc, argv);

	/*pixmap.readBMPFile("feup.bmp");
	pixmap.setTexture(1);*/

	if( init() < 0 ) exit(0);

	GLUI_Master.set_glutReshapeFunc( myGlutReshape );

	arVideoCapStart();

	glutMotionFunc( motionEvent );

	argMainLoop( mouseEvent, keyEvent, mainLoop );

	return (0);
}

static bool connect(void) {
	if ((retval = WSAStartup(0x202, &wsaData)) != 0)
    {
       fprintf(stderr,"Client: WSAStartup() failed with error %d\n", retval);
	   WSACleanup();
	   return false;
    } else printf("Client: WSAStartup() is OK.\n");

	// Attempt to detect if we should call gethostbyname() or gethostbyaddr()
    if (isalpha(server_name[0]))
    {   // server address is a name
        hp = gethostbyname(server_name);
    } else { // Convert nnn.nnn address to a usable one
        addr = inet_addr(server_name);
        hp = gethostbyaddr((char *)&addr, 4, AF_INET);
    }

    if (hp == NULL ) {
        fprintf(stderr,"Client: Cannot resolve address \"%s\": Error %d\n", server_name, WSAGetLastError());
        WSACleanup();
        return false;
    } else printf("Client: gethostbyaddr() is OK.\n");

    // Copy the resolved information into the sockaddr_in structure
    memset(&server, 0, sizeof(server));
    memcpy(&(server.sin_addr), hp->h_addr, hp->h_length);
    server.sin_family = hp->h_addrtype;
    server.sin_port = htons(port);
 
	// Open a socket
    conn_socket = socket(AF_INET, socket_type, 0); 
    if (conn_socket < 0 )
    {
        fprintf(stderr,"Client: Error Opening socket: Error %d\n", WSAGetLastError());
        WSACleanup();
        return false;
    } else printf("Client: socket() is OK.\n");
 
    // This code is specific to UDP or TCP.
    //    When connect() is called on a datagram socket, it does not
    //    actually establish the connection as a stream (TCP) socket
    //    would. Instead, TCP/IP establishes the remote half of the
    //    (LocalIPAddress, LocalPort, RemoteIP, RemotePort) mapping.
    //    This enables the use of send() and recv() on datagram sockets,
    //    instead of recvfrom() and sendto()
    printf("Client: Client connecting to: %s.\n", hp->h_name);
    if (connect(conn_socket, (struct sockaddr*)&server, sizeof(server)) == SOCKET_ERROR)
    {
        fprintf(stderr,"Client: connect() failed: %d\n", WSAGetLastError());
        WSACleanup();
        return false;
    }
    else printf("Client: connect() is OK.\n");

	//buffer = (char*) malloc (512);
	//printf("Client: Received %d bytes, data \"%s\" from server.\n", retval, string);

	//sendmsg("ARMotion");

	return true;
}

static bool sendmsg(char *msg) {

    retval = send(conn_socket, msg, strlen(msg), 0);
    if (retval == SOCKET_ERROR)
    {
        fprintf(stderr,"Client: send() failed: error %d.\n", WSAGetLastError());
        return false;
    } else printf("Client: send() is OK.\n");

    printf("Client: Sent data %s \n", msg);
    
    if (retval == 0) {
        printf("Client: Server closed connection.\n");
        return false;
    }

	return true;
}

static void close(void) {
	//sendmsg("exit");
	closesocket(conn_socket);
    WSACleanup();
}

/* set up the application parameters - read in from command line*/
static int init(void)
{
	char     cparaname[256];
	char     pattname[256];
	char     pattname2[256];
	ARParam  wparam;

	strcpy( cparaname, "Data/camera_para.dat" );
	strcpy( pattname,  "Data/patt.hiro" );
	strcpy( pattname2, "Data/patt.kanji" );

	/* open the video path */
	if( arVideoOpen( vconf ) < 0 ) exit(0);
	/* find the size of the window */
	if( arVideoInqSize(&xsize, &ysize) < 0 ) exit(0);
	printf("Image size (x,y) = (%d,%d)\n", xsize, ysize);

	/* set the initial camera parameters */
	if( arParamLoad(cparaname, 1, &wparam) < 0 ) {
		printf("Camera parameter load error !!\n");
		exit(0);
	}
	arParamChangeSize( &wparam, xsize, ysize, &cparam );
	arInitCparam( &cparam );
	printf("*** Camera Parameter ***\n");
	arParamDisp( &cparam );

	/* open the graphics window */
	argInit( &cparam, 1.0, 0, 2, 1, 0 );

	if( (target_id = arLoadPatt(pattname)) < 0 ) {
		printf("Target pattern load error!!\n");
		exit(0);
	}

	if( (target_id2 = arLoadPatt(pattname2)) < 0 ) {
		printf("Target pattern load error!!\n");
		exit(0);
	}

	arDebug = 1;

	glQ = gluNewQuadric();

	return 0;
}

/* cleanup function called when program exits */
static void cleanup(void)
{
	close();
	arVideoCapStop();
	arVideoClose();
	argCleanup();
}

static void keyEvent( unsigned char key, int x, int y)
{
	switch (key) {
	case 0x1B:
	case 'Q':
	case 'q':
		cleanup();
		exit(0);
		break;
	case 'D':
	case 'd':
		arDebug = !arDebug;
		break;
	case 'H':
	case 'h':
		printf("Keys:\n");
		printf(" q or [esc]    Quit demo.\n");
		printf(" d             Activate / deactivate debug mode.\n");
		printf(" h	           Show this help.\n");
		printf("\nAdditionally, the ARVideo library supplied the following help text:\n");
		arVideoDispOption();
		break;
	default:
		break;
	}

	//glutPostRedisplay();
}

static void mouseEvent(int button, int state, int x, int y)
{
	if( state == GLUT_UP ) {
		mouse_st = 0;
	}
	else if( state == GLUT_DOWN ) {
		if( button == GLUT_LEFT_BUTTON ) {
			mouse_st = 1;
			mouse_ox = x;
			mouse_oy = y;
		}
		/*else if( button == GLUT_MIDDLE_BUTTON ) {
		disp_mode = 1 - disp_mode;
		}*/
		else if( button == GLUT_RIGHT_BUTTON ) {
			mouse_st = 2;
			mouse_ox = x;
			mouse_oy = y;
		}
	}

	//glutPostRedisplay();
}

static void motionEvent( int x, int y )
{
	if( mouse_st == 1 ) {
		a += ((double)x - mouse_ox) / 2.0;
		b -= ((double)y - mouse_oy) / 2.0;
		if( a <   0.0 ) a += 360.0;
		if( a > 360.0 ) a -= 360.0;
		if( b < -90.0 ) b =  -90.0;
		if( b >   0.0 ) b =    0.0;
	}
	else if( mouse_st == 2 ) {
		r *= (1.0 + ((double)y - mouse_oy)*0.01);
		if( r < 10.0 ) r = 10.0;
	}

	mouse_ox = x;
	mouse_oy = y;

	//glutPostRedisplay();
}

static void mainLoop(void)
{
	ARUint8         *dataPtr;
	ARMarkerInfo    *marker_info;
	int             marker_num;
	int             j, k, q;

	/* grab a vide frame */
	if( (dataPtr = (ARUint8 *)arVideoGetImage()) == NULL ) {
		arUtilSleep(2);
		return;
	}

	glClearColor( 0.0, 0.0, 0.0, 0.0 );
	glClearDepth( 1.0 );
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	argDrawMode2D();
	argDispImage( dataPtr, 0, 0 );

	/* detect the markers in the video frame */
	if( arDetectMarker(dataPtr, thresh, &marker_info, &marker_num) < 0 ) {
		cleanup();
		exit(0);
	}
	arVideoCapNext();

	/* if the debug mode is on draw squares 
	around the detected squares in the video image */
	if( arDebug ) {
		if( arImageProcMode == AR_IMAGE_PROC_IN_HALF )
			argDispHalfImage( arImage, 2, 1 );
		else
			argDispImage( arImage, 2, 1);
	}

	if (glon) {
		glon=false;
		argDrawMode3D();
		//argDraw3dCamera( 1, 1 );

		glui = GLUI_Master.create_glui_subwindow (1, GLUI_SUBWINDOW_RIGHT );

		view_rot = glui->add_rotation( "Rotation", view_rotate );
		view_rot->set_spin( 1.0 );
		GLUI_Button *resrot_button = glui->add_button("Reset Camera", 1,callback);

		/*glui->add_separator();
		trans_z = glui->add_translation( "Zoom", GLUI_TRANSLATION_Z, &obj_pos[2] );
		trans_z->set_speed( 1 );*/
		
		glui->add_separator();
		//glui->add_spinner_to_panel(panel_main,"Eixos");

		GLUI_Rollout *opcoes = glui->add_rollout("Motion Menu");

		GLUI_Rollout *alg = glui->add_rollout_to_panel(opcoes,"Algorithm");

		GLUI_Spinner * ins1 = glui->add_spinner_to_panel(alg,"Instrument1",2,&inst1);
		GLUI_Spinner * ins2 = glui->add_spinner_to_panel(alg,"Instrument2",2,&inst2);

		GLUI_RadioGroup * modo = glui->add_radiogroup_to_panel(alg,&atype);
		glui->add_radiobutton_to_group( modo, "Manual");
		glui->add_radiobutton_to_group( modo, "Audio Drums");
		glui->add_radiobutton_to_group( modo, "Bass Mutation");
		glui->add_radiobutton_to_group( modo, "Chaos");
		glui->add_radiobutton_to_group( modo, "Drum Pattern");
		glui->add_radiobutton_to_group( modo, "Drunk Walk");
		glui->add_radiobutton_to_group( modo, "Fractal");
		glui->add_radiobutton_to_group( modo, "Jm808");
		glui->add_radiobutton_to_group( modo, "ModeMelody");
		glui->add_radiobutton_to_group( modo, "Mozart");
		glui->add_radiobutton_to_group( modo, "MuchChaos");
		glui->add_radiobutton_to_group( modo, "PianoPhase");
		glui->add_radiobutton_to_group( modo, "RetroInv");
		glui->add_radiobutton_to_group( modo, "RhythmicAutomata");
		glui->add_radiobutton_to_group( modo, "RowYourBoat");
		glui->add_radiobutton_to_group( modo, "SineMelody");
		glui->add_radiobutton_to_group( modo, "Stochastic");

		GLUI_Spinner * len = glui->add_spinner_to_panel(alg,"Length",2,&length);

		GLUI_Checkbox *check = glui->add_checkbox_to_panel(opcoes,"Beat",&beat);

		GLUI_Button *setup_button = glui->add_button_to_panel(opcoes,"Setup",3,callback);
		GLUI_Button *play_button = glui->add_button_to_panel(opcoes,"Play",4,callback);
		GLUI_Button *stop_button = glui->add_button_to_panel(opcoes,"Stop",5,callback);
		GLUI_Button *exit_button = glui->add_button_to_panel(opcoes,"Exit",2,callback);
		/*
		GLUI_Rollout *inst = glui->add_rollout("Instructions");
		glui->add_statictext_to_panel(inst,"1-Setup generating method");
		glui->add_statictext_to_panel(inst,"2-Wave around");

		GLUI_Rollout *creditos = glui->add_rollout("Credits");
		glui->add_statictext_to_panel(creditos,"Vitor Ribeiro");
		*/

		GLUI_Master.set_glutKeyboardFunc( keyEvent );
		GLUI_Master.set_glutMouseFunc( mouseEvent );
		glutMotionFunc( motionEvent );
		GLUI_Master.set_glutIdleFunc( myGlutIdle );
		GLUI_Master.set_glutSpecialFunc( NULL );
		//GLUI_Master.sync_live_all();

		glui->set_main_gfx_window( 1 );

		argDrawMode2D();	

	}

	/* check for object visibility */
	k = -1;
	q = -1;

	for( j = 0; j < marker_num; j++ ) {

		if (marker_info[j].id == target_id  ){
			if( k == -1 ) k = j;
			else {
				if( marker_info[k].cf < marker_info[j].cf ) k = j;
			}
		}

		if (marker_info[j].id == target_id2 ){
			if( q == -1 ) q = j;
			else {
				if( marker_info[q].cf < marker_info[j].cf ) q = j;
			}
		}

	}

	if( k != -1 ) {
		glDisable(GL_DEPTH_TEST);
		getResult( &marker_info[k] , 1);
	}

	if ( q!=-1 ) {
		glDisable(GL_DEPTH_TEST);
		getResult( &marker_info[q] , 2);
	}

	if( arDebug ) {
		draw_exview( a, b, r, target_trans, 1, 1 );
	}

	//Render objects in the same coordinate space of AR marker 	
	//argConvGlpara(target_trans, myScene->markerTrans);
	/*argDrawMode3D();
	argDraw3dCamera( 0, 0 );
	myOpenGL->RenderMarkerObjs(myScene);
	argDrawMode2D();*/

	glutPostRedisplay();

	argSwapBuffers();
}

static void getResult( ARMarkerInfo *marker_info , int player)
{
	/*
	double      target_trans[3][4];
    double      cam_trans[3][4];
    char        string[256];
	*/

	if( arGetTransMat(marker_info, target_center, target_width, target_trans) < 0 ) return;
	if( arUtilMatInv(target_trans, cam_trans) < 0 ) return;
	if( arUtilMat2QuatPos(cam_trans, quat, pos) < 0 ) return;

	sprintf(string,"{\"mark\":\"%i\",\"x\":\"%3.1f\",\"y\":\"%3.1f\",\"z\":\"%3.1f\"}", 
		player, cam_trans[0][3], cam_trans[1][3], cam_trans[2][3]);
	sendmsg(string);

	draw( "target", target_trans, 0, 0 , player);

	sprintf(string,"Mark %i : Pos x: %3.1f  y: %3.1f  z: %3.1f",
            player, cam_trans[0][3], cam_trans[1][3], cam_trans[2][3]);

	if (arDebug) print_string( string , player );

	return;
}

static void callback(int id){
	switch (id){
	case 1:
		for (int f=0;f!=16;f++)	view_rotate[f]=iden_matrix[f];
		break;
	case 2:
		cleanup();
		exit(0);
		break;

	case 3:
		sprintf(string,"{\"type\":\"%i\",\"inst1\":\"%i\",\"inst2\":\"%i\",\"length\":\"%i\",\"beat\":\"%i\"}", 
		atype,inst1,inst2,length,beat);
		sendmsg(string);
		break;	
	case 4:
		sprintf(string,"{\"play\":\"1\"}");
		sendmsg(string);
		break;	
	case 5:
		sprintf(string,"{\"play\":\"0\"}");
		sendmsg(string);
		break;	
	default:
		break;
	}
}

static void myGlutReshape(int w, int h)
{
	int tx, ty, tw, th;

	GLUI_Master.get_viewport_area( &tx, &ty, &tw, &th );
	glViewport( tx, ty, tw, th );
	xy_aspect = (float)tw / (float)th;

	//GLUI_Master.auto_set_viewport();
	//glutSetWindow(1);

	glutPostRedisplay();

}

static void myGlutIdle( void ) {

	glutSetWindow(1);
	glutPostRedisplay();
}

/* GL */

static void setup_light()
{
	static int  mat_f = 1;
	GLfloat     mat_amb_diff[]  = {0.9, 0.9, 0.0, 1.0};
	GLfloat     mat_specular[]  = {0.5, 0.5, 0.5, 1.0};
	GLfloat     mat_shininess[] = {10.0};
	GLfloat     light_ambient[] = { 0.01, 0.01, 0.01, 1.0 };
	GLfloat     light_diffuse[] = { 1.0, 1.0, 1.0, 1.0 };
	GLfloat     light_specular[] = { 1.0, 1.0, 1.0, 1.0 };
	GLfloat     light_position[] = { 100.0, 300.0, 700.0, 1.0 };

	if( mat_f ) {
		glMaterialfv(GL_FRONT, GL_AMBIENT_AND_DIFFUSE, mat_amb_diff);
		glMaterialfv(GL_FRONT, GL_SPECULAR, mat_specular);
		glMaterialfv(GL_FRONT, GL_SHININESS, mat_shininess);	
		glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);
		glEnable(GL_COLOR_MATERIAL);
		mat_f = 0;
	}

	glLightfv(GL_LIGHT0, GL_AMBIENT, light_ambient);
	glLightfv(GL_LIGHT0, GL_DIFFUSE, light_diffuse);
	glLightfv(GL_LIGHT0, GL_SPECULAR, light_specular);
	glLightfv(GL_LIGHT0, GL_POSITION, light_position);
}

static void print_string( char *string , int player )
{
	int i=0;

	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();

	// display the position data
	if (player==1) {
		glTranslatef(-0.95, -0.10, 0.0);
	} else if (player==2) {
		glTranslatef(-0.95, -0.20, 0.0);
	} else {
		glTranslatef(-0.95, -0.30, 0.0);
	}
	

	// draw red text on the polygon
	glColor3f(0.75, 0.0, 0.0);
	glRasterPos2i(0.0, 0.0);
	for (i=0; i<(int)strlen(string); i++) {
		if(string[i] != '\n' ) {
			glutBitmapCharacter(GLUT_BITMAP_TIMES_ROMAN_24, string[i]);
		}
		else {
			glTranslatef(0.0, -0.07, 0.0);
			glRasterPos2i(0.0, 0.0);
		}
	}

	return;
}

static int draw_exview( double a, double b, double r, double trans[3][4], int xwin, int ywin )
{
	double      vtrans[3][4];
	double      gl_para[16];
	int         i, j;

	argDrawMode3D();
	argDraw3dCamera( xwin, ywin );
	glDepthFunc(GL_LEQUAL);
	glEnable(GL_DEPTH_TEST);
	glEnable(GL_NORMALIZE);

	get_trans( a, b, r, vtrans );
	argConvGlpara(vtrans, gl_para);
	glMatrixMode(GL_PROJECTION);
	glMultMatrixd( gl_para );

	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	setup_light();

	glPushMatrix();
	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);

	for( j = -300; j <= 200; j+= 100 ) {
		for( i = -300; i <= 200; i+= 100 ) {
			glBegin(GL_QUADS);
			glNormal3f( 0.0, 0.0, 1.0 );
			if( (j/100+i/100)%2 ) glColor4f( 0.6, 0.6, 0.6, 1.0 );
			else                 glColor4f( 0.0, 0.3, 0.0, 1.0 );
			glVertex3f( i,     j,     0.0 );
			glVertex3f( i,     j+100, 0.0 );
			glVertex3f( i+100, j+100, 0.0 );
			glVertex3f( i+100, j,     0.0 );
			glEnd();
		}
	}
	draw_axis();

	/*glColor4f( 0.0, 0.0, 0.5, 1.0 );
	glTranslatef( 0.0, 0.0, 25.0 );
	glutSolidCube(50.0);*/

	glDisable( GL_LIGHTING );
	glPopMatrix();

	draw_camera( trans );

	glDisable(GL_NORMALIZE);
	glDisable( GL_DEPTH_TEST );
	argDrawMode2D();

	return 0;
}

static void get_trans( double a, double b, double r, double trans[3][4] )
{
	ARMat   *mat;
	double  sa, ca, sb, cb;
	double  x, y, z;
	int     i, j;

	sa = sin(a*3.141592/180.0);
	ca = cos(a*3.141592/180.0);
	sb = sin(b*3.141592/180.0);
	cb = cos(b*3.141592/180.0);

	x = 0.0;
	y = -r * cb;
	z = -r * sb;

	mat = arMatrixAlloc( 4, 4 );

	mat->m[0*4+0] =  ca;
	mat->m[0*4+1] =  sa*sb;
	mat->m[0*4+2] =  sa*cb;
	mat->m[1*4+0] = -sa;
	mat->m[1*4+1] =  ca*sb;
	mat->m[1*4+2] =  ca*cb;
	mat->m[2*4+0] =  0;
	mat->m[2*4+1] = -cb;
	mat->m[2*4+2] =  sb;
	mat->m[0*4+3] =  x*ca + y*sa;
	mat->m[1*4+3] = -x*sa + y*ca;
	mat->m[2*4+3] = z;
	mat->m[3*4+0] = 0;
	mat->m[3*4+1] = 0;
	mat->m[3*4+2] = 0;
	mat->m[3*4+3] = 1;

	arMatrixSelfInv( mat );
	for( j = 0; j < 3; j++ ) {
		for( i = 0; i < 4; i++ ) {
			trans[j][i] = mat->m[j*4+i];
		}
	}
	arMatrixFree( mat );

	return;
}

static void draw_axis( void )
{
	glPushMatrix();
	glRotatef( 90.0, 0.0, 1.0, 0.0 );
	glColor4f( 1.0, 0.0, 0.0, 1.0 );
	glutSolidCone(5.0, 100.0, 20, 24);
	glPopMatrix();

	glPushMatrix();
	glRotatef( -90.0, 1.0, 0.0, 0.0 );
	glColor4f( 0.0, 1.0, 0.0, 1.0 );
	glutSolidCone(5.0, 100.0, 20, 24);
	glPopMatrix();

	glPushMatrix();
	glRotatef( 00.0, 0.0, 0.0, 1.0 );
	glColor4f( 0.0, 0.0, 1.0, 1.0 );
	glutSolidCone(5.0, 100.0, 20, 24);
	glPopMatrix();

	return;
}

static void draw_camera( double trans[3][4] )
{
	/*
	double      gl_para[16];
	*/
	double      btrans[3][4];
	double      quat[4], pos[3], angle;

	arUtilMatInv( trans, btrans );

	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	arUtilMat2QuatPos( btrans, quat, pos );
	angle = -acos(quat[3])*360.0/3.141592;
	glTranslatef( pos[0], pos[1], pos[2] );
	glRotated( angle, quat[0], quat[1], quat[2] );
	/*
	argConvGlpara(btrans, gl_para);
	glMultMatrixd( gl_para );
	*/

	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);

	glPushMatrix();
	glColor4f( 0.9, 0.9, 0.0, 1.0 );
	glTranslatef( 0.0, 0.0, -10.0 );
	glScalef( 10.0, 10.0, 20.0 );
	glutSolidCube(1.0);
	glPopMatrix();

	glColor4f( 0.9, 0.0, 0.9, 1.0 );
	glPushMatrix();
	glTranslatef( 0.0, 0.0, -40.0 );
	glScalef( 30.0, 30.0, 50.0 );
	glutSolidCube(1.0);
	glPopMatrix();

	glDisable( GL_LIGHTING );
	glPopMatrix();

	return;
}

static int draw( char *name, double trans[4][4], int xwin, int ywin , int player)
{
	double      gl_para[16];

	argConvGlpara(trans, gl_para);

	draw_object( name, gl_para, xwin, ywin , player);

	return(0);
}

/* draw the user object */
static int draw_object( char *name, double gl_para[16], int xwin, int ywin , int player)
{
	argDrawMode3D();
	argDraw3dCamera( xwin, ywin );

	glDepthFunc(GL_LEQUAL);
	glEnable(GL_DEPTH_TEST);
	glEnable(GL_NORMALIZE);
	/* load the camera transformation matrix */
	glMatrixMode(GL_PROJECTION);
	glMultMatrixd( gl_para );

	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
	setup_light();
	glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);

	// desenho 
	if( strcmp(name, "target") == 0 ) {
		glMultMatrixf( view_rotate );

		if (player==1) {
			glLoadIdentity();
			glMultMatrixf( view_rotate );
			glColor3f(0, 0, 1);
			drawClosedCylinder(glQ,25,25,10,25,25,1,1);
		} else if (player==2) {
			glLoadIdentity();
			glMultMatrixf( view_rotate );
			glColor3f(0, 1, 0);
			drawClosedCylinder(glQ,25,25,10,25,25,1,1);
		} else {
			glLoadIdentity();
			glMultMatrixf( view_rotate );
			glColor3f(1, 0, 0);
			drawClosedCylinder(glQ,25,25,10,25,25,1,1);
		}
	}
	else {
		printf("unknown object type!!\n");
	}

	glDisable( GL_LIGHTING );
	glDisable( GL_NORMALIZE );
	glDisable( GL_DEPTH_TEST );
	argDrawMode2D();

	return 0;
}

static void newellSquare(GLdouble *vec1,GLdouble *vec2,GLdouble *vec3,GLdouble *vec4,GLdouble *normal)
{
	normal[0] = (vec1[1]-vec2[1])*(vec1[2]+vec2[2]) + 
		(vec2[1]-vec3[1])*(vec2[2]+vec3[2]) + 
		(vec3[1]-vec4[1])*(vec3[2]+vec4[2]) +
		(vec4[1]-vec1[1])*(vec4[2]+vec1[2]);
	normal[1] = (vec1[2]-vec2[2])*(vec1[0]+vec2[0]) + 
		(vec2[2]-vec3[2])*(vec2[0]+vec3[0]) + 
		(vec3[2]-vec4[2])*(vec3[0]+vec4[0]) +
		(vec4[2]-vec1[2])*(vec4[0]+vec1[0]);
	normal[2] = (vec1[0]-vec2[0])*(vec1[1]+vec2[1]) + 
		(vec2[0]-vec3[0])*(vec2[1]+vec3[1]) + 
		(vec3[0]-vec4[0])*(vec3[1]+vec4[1]) +
		(vec4[0]-vec1[0])*(vec4[1]+vec1[1]);

	normalise(normal);

}

static void normalise(GLdouble *vec)
{
	GLdouble length = 0.0;
	int i;

	for (i=0;i<3;i++){
		length += vec[i]*vec[i]; 
	}
	length= (GLdouble) sqrt((double)length); 

	for (i=0;i<3;i++){
		vec[i] = vec[i]/length; 
	}
}

static void drawBox(GLdouble dimx, GLdouble dimy, GLdouble dimz, float s_factor, float t_factor)
{

	GLdouble dx=dimx/2, dy=dimy/2, dz=dimz/2;

	GLdouble v1[3] = {dx,-dy,dz};
	GLdouble v2[3] = {dx,-dy,-dz};
	GLdouble v3[3] = {dx,dy,dz};
	GLdouble v4[3] = {dx,dy,-dz};
	GLdouble v5[3] = {-dx,-dy,dz};
	GLdouble v6[3] = {-dx,dy,dz};
	GLdouble v7[3] = {-dx,dy,-dz};
	GLdouble v8[3] = {-dx,-dy,-dz};
	GLdouble normal[3];

	//glColor3f(1, 1, 0);
	//float mat_shininess[] = {20.0}; /* How shiny is the object (specular exponent)  */
	//float mat_specular[] = {0.3, 0.3, 0.3, 0.3}; /* specular reflection. */
	//float mat_diffuse[] = {0.3, 0.3, 0.3, 0.3}; /* diffuse reflection. */
	//float amb[] = {0.5, 0.5, 0.5, 0.5}; /* diffuse reflection. */
	// define as caracteristicas do material (dos materiais seguintes, i.e. ate nova alteracao
	/*glMaterialfv(GL_FRONT, GL_SHININESS, mat_shininess);
	glMaterialfv(GL_FRONT, GL_SPECULAR, mat_specular);
	glMaterialfv(GL_FRONT, GL_DIFFUSE, mat_diffuse);
	glMaterialfv(GL_FRONT, GL_AMBIENT, amb);
	glDisable(GL_COLOR_MATERIAL);*/

	//Face frente - 0
	glEnable(GL_TEXTURE_2D);
	newellSquare(v5,v6,v3,v1,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v1);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v3);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v6);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v5);
	glEnd();

	// face anterior - 1
	newellSquare(v8,v7,v4,v2,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v8);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v7);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v4);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v2);
	glEnd();

	// face lateral - 2
	newellSquare(v2,v4,v3,v1,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v2);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v4);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v3);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v1);
	glEnd();

	newellSquare(v5,v6,v7,v8,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v5);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v6);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v7);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v8);
	glEnd();

	// base
	newellSquare(v1,v5,v8,v2,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v1);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v5);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v8);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v2);
	glEnd();

	// topo 
	newellSquare(v3,v4,v7,v6,normal);
	glBegin(GL_POLYGON);
	glNormal3dv(normal);
	glTexCoord2f(0.0,0.0);			glVertex3dv(v3);
	glTexCoord2f(0.0,t_factor);		glVertex3dv(v4);
	glTexCoord2f(s_factor,t_factor);glVertex3dv(v7);
	glTexCoord2f(s_factor,0.0);		glVertex3dv(v6);
	glEnd();
	glDisable(GL_TEXTURE_2D);

}

static void drawClosedCylinder(GLUquadric* glQ,	GLdouble base, GLdouble top, GLdouble height, GLint slices, GLint stacks, bool drawbase, bool drawtop)
{
	//criar cilindro
	gluCylinder(glQ, base,top,height,slices,stacks);
	//fechar cilindro
	if(drawtop){
		glPushMatrix();
		glTranslatef(0,0,height);
		gluDisk(glQ, 0, top, slices, 4);
		glPopMatrix();
	}
	if(drawbase){
		glPushMatrix();
		gluQuadricOrientation(glQ, GLU_INSIDE);
		glTranslatef(0,0,0);
		gluDisk(glQ, 0, top, slices, 4);
		glPopMatrix();
	}
}
