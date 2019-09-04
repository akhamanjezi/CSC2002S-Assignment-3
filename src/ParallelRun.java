import java.util.concurrent.RecursiveTask;

public class ParallelRun extends RecursiveTask<CloudData>  {
	  int lo; // arguments
	  int hi;
	  static final int SEQUENTIAL_CUTOFF=3000;
	  int divpoint;
	  CloudData data;

//	  Vector ans = new Vector(0,0, 0); // result
//	  OutputObject result = new OutputObject(prevWind, data);

	  ParallelRun(CloudData data, int l, int h) {
	    lo=l; hi=h; this.data = data;
	  }


	  protected CloudData compute(){// return answer - instead of run
		  if((hi-lo) < SEQUENTIAL_CUTOFF) {
			  Vector winds = new Vector();
			  int classcount = lo%3;
			  int at = lo/3;
			  at = at%(data.dimx*data.dimy);
			  int left = -3;
			  int right = 3;
			  int above = -data.dimx*3;
			  int bottom = data.dimx*3;
			  int x, y, u;
			  boolean aboveleft, aboveright, bottomleft,bottomright;
			  for (int i = lo; i < hi; i+=3) {
				  x = i;
				  y = i + 1;
				  u = i + 2;
				  aboveleft = false;
				  aboveright = false;
				  bottomleft = false;
				  bottomright = false;

				  winds.x += data.linAdvection[x];
				  winds.y += data.linAdvection[y];
				  winds.n++;

				  data.prevWind.x += data.linAdvection[x];
				  data.prevWind.y += data.linAdvection[y];
				  data.prevWind.n++;

				  if(at%data.dimx!=0){
					  //add left
					  winds.x += data.linAdvection[x+left];
					  winds.y += data.linAdvection[y+left];
					  winds.n++;
					  if(at >= data.dimx){
						  //add above left
						  winds.x += data.linAdvection[x+left+above];
						  winds.y += data.linAdvection[y+left+above];
						  winds.n++;
						  aboveleft = true;
					  }
					  if((at+data.dimx)<data.dimx*data.dimy){
						  //add bottom left
						  winds.x += data.linAdvection[x+left+bottom];
						  winds.y += data.linAdvection[y+left+bottom];
						  winds.n++;
						  bottomleft = true;
					  }
				  }
				  if(at >= data.dimx){
					  //add above
					  winds.x += data.linAdvection[x+above];
					  winds.y += data.linAdvection[y+above];
					  winds.n++;
					  if(at%data.dimx!=0 && aboveleft == false) {
						  //add above left
						  winds.x += data.linAdvection[x+above+left];
						  winds.y += data.linAdvection[y+above+left];
						  winds.n++;
					  }
					  if((at+1)%data.dimx!=0) {
						  //add above right
						  winds.x += data.linAdvection[x+above + right];
						  winds.y += data.linAdvection[y+above + right];
						  winds.n++;
						  aboveright = true;
					  }
				  }
				  if((at+1)%data.dimx!=0){
					  //add right
					  winds.x += data.linAdvection[x+right];
					  winds.y += data.linAdvection[y+right];
					  winds.n++;
					  if((at+data.dimx)<data.dimx*data.dimy){
						  //add bottom right
						  winds.x += data.linAdvection[x+right+bottom];
						  winds.y += data.linAdvection[y+right+bottom];
						  winds.n++;
						  bottomright=true;
					  }
					  if(at >= data.dimx && aboveright==false){
						  //add above right
						  winds.x += data.linAdvection[x+above+right];
						  winds.y += data.linAdvection[y+above+right];
						  winds.n++;
					  }
				  }
				  if((at+data.dimx)<data.dimx*data.dimy){
					  //add bottom
					  winds.x += data.linAdvection[x+bottom];
					  winds.y += data.linAdvection[y+bottom];
					  winds.n++;
					  if(at%data.dimx!=0 && bottomleft==false) {
						  //add bottom left
						  winds.x += data.linAdvection[x+bottom+left];
						  winds.y += data.linAdvection[y+bottom+left];
						  winds.n++;
					  }
					  if((at+1)%data.dimx!=0 && bottomright==false) {
						  //add bottom right
						  winds.x += data.linAdvection[x+bottom+right];
						  winds.y += data.linAdvection[y+bottom+right];
						  winds.n++;
					  }
				  }
				  at+=1;
				  if(at == data.dimx*data.dimy){
					  at = 0;
				  }

				  Vector wind = winds.getAverage();

				  if (Math.abs(data.linAdvection[u]) > wind.mag()){
					  data.linClassification[classcount++] = 0;
				  }else if(wind.mag() > 0.2){
					  data.linClassification[classcount++] = 1;
				  }else{
					  data.linClassification[classcount++] = 2;
				  }

				  winds.n=0;
				  winds.x = 0;
				  winds.y = 0;

			  }
		      return data;
		  }
		  else {
		  	  divpoint = (hi+lo)/2;
		  	  while ((divpoint)%3!=0){
		  	  	divpoint++;
			  }
			  ParallelRun left = new ParallelRun(data,lo,divpoint);
			  ParallelRun right = new ParallelRun(data,divpoint,hi);
//
			  // order of next 4 lines
			  // essential – why?
			  left.fork();
			  CloudData rightAns = right.compute();
			  CloudData leftAns  = left.join();
			  leftAns.prevWind.toAdd(rightAns.prevWind);
			  int start = divpoint/3;
			  for (int i = divpoint; i < hi; i+=3) {
				  leftAns.linClassification[start] = rightAns.linClassification[start];
				  start++;
			  }
			  return leftAns;
		  }
	 }
}


