#!/usr/bin/env groovy

// just a build bot test (I need a commit)

// CONF
def svnDir = new File('/home/annis/svn/')
def contextFile = new File('/etc/jetty/contexts/annis-dev.xml')
def webappOffline = new File('/srv/jetty/offline/')
def webappAnnis = new File('/srv/jetty/Annis-dev/')
def annisHome = new File('/opt/annis-dev')

// check if other instance is running
def lockFile = new File('/tmp/korpling_buildbot.lock')


if(lockFile.exists())
{
  println 'Error: other instance is running.'
  return 0
}
else
{
  lockFile.createNewFile()
  lockFile.deleteOnExit()
}

def shouldBuild = true
def newRev = -1
def oldRev = -1

// get revision of last build
def revFile = new File('/tmp/korpling_buildbot.svnrev')
if(revFile.exists())
{
  shouldBuild = false
  def lines = revFile.readLines()
  if(lines.size() > 0)
  {
    oldRev = Integer.parseInt(lines[0])
      
  }
}

// read in new revision
print('getting svn info...')
proc = "svn info https://korpling.german.hu-berlin.de/svn/annis".execute(null, svnDir)
proc.waitFor()
lines = proc.text.tokenize('\n\r')
lines.each
{
  if(it.startsWith('Revision: '))
  {
    newRev = Integer.parseInt(it.substring('Revision: '.length()))
    if(newRev != oldRev)
    {
      shouldBuild = true
    }
  }
} 
println(' finished')

if(shouldBuild)
{

  // update svn
  print('updating svn...')
  def proc = "svn update".execute(null, svnDir)
  proc.waitFor()
  checkError(proc)
  proc = "svn revert -R .".execute(null, svnDir)
  proc.waitFor()
  checkError(proc)
  println(" finished")

  def serviceDir = new File(svnDir.absolutePath + '/AnnisService/trunk')
  def webappDir = new File(svnDir.absolutePath + '/Annis2-web')
  println("updating installation because of new revision (${oldRev} -> ${newRev})")
  
  build('cleaning service', 'ant clean', serviceDir)
  build('build parsers for service', 'ant build-parsers', serviceDir)
  build('packaging service', 'ant create-jar', serviceDir);
  build('packaging rmi (service)', 'ant create-rmi-jars', serviceDir);
  
  def props = '-Dj2ee.platform.classpath=/opt/jetty/lib/servlet-api-2.5-6.1.14.jar'
  build('cleaning webapp', 'ant ' + props + ' clean', webappDir)
  build('building and packaging webapp', 'ant ' + props + ' dist', webappDir)

  print('shutting down web-application...')  
  def fwContext = new FileWriter(contextFile.absolutePath)
  fwContext.write("" + context("Annis-dev", webappOffline.absolutePath))
  fwContext.close()
  println(' finished')
  
  print('shutting down service...')
  proc = "${annisHome.absolutePath}/bin/annis-service.sh stop".execute()
  proc.waitFor()
  if(checkError(proc))
    println(' finished')


  print('deploying web-application...')
  proc = ['bash', '-c', "rm -R ${webappAnnis.absolutePath}/*"].execute()
  proc.waitFor()
  checkError(proc)
  proc = "unzip -o ${webappDir.absolutePath}/dist/Annis2-web.war".execute(null, webappAnnis)
  proc.waitFor()
  checkError(proc)
  proc = "chmod -R ug+rw ${webappAnnis.absolutePath}".execute()
  proc.waitFor()
  checkError(proc)
  println(' finished') 

  // replace revision in javascript config file
  print('adjusting JS-config file...')
  def jsConfFile = new File(webappAnnis.absolutePath + '/javascript/annis/config.js')
  def oldConf= jsConfFile.getText()
  def newConf = oldConf.replace('${SVN_REVISION}','' + newRev)
  def fwConf = new FileWriter(jsConfFile)
  fwConf.write(newConf)
  fwConf.close()
  println(' finished')

  print('copying rmi jar (1)...')
  proc = "cp ${serviceDir.absolutePath}/annis-rmi-service-1.0.jar ${webappAnnis.absolutePath}/WEB-INF/lib".execute()
  proc.waitFor()
  if(checkError(proc))
    println('finished')

  print('copying rmi jar (2)...')
  proc = "cp ${serviceDir.absolutePath}/annis-rmi-objects-1.0.jar ${webappAnnis.absolutePath}/WEB-INF/lib".execute()
  proc.waitFor()
  if(checkError(proc))
    println('finished')
  print('deploying service...')
  proc = "cp ${serviceDir.absolutePath}/annis.jar ${annisHome.absolutePath}/lib/".execute()
  proc.waitFor()  
  if(checkError(proc))
    println(' finished')

  print('starting service...')
  proc = "${annisHome.absolutePath}/bin/annis-service.sh start".execute()
  proc.waitFor()
  if(proc.exitValue() != 0)
  {
    println(' ERROR')
    println("${proc.in.text}\n\n${proc.err.text}")
  }
  println(' finished')
  
  print('starting web-application...')  
  fwContext = new FileWriter(contextFile.absolutePath)
  fwContext.write(context("Annis-dev", webappAnnis.absolutePath))
  fwContext.close()
  println(' finished')

  println(' finished')

  // write out revision file
  def fwRev = new FileWriter(revFile)
  fwRev.write("" + newRev)
  fwRev.close()

  println('FINISHED')  
}
else
{
  println('no build needed')
}




public void build(String desc, String cmd, File dir)
{
  print("building target \"${desc}\"...")
  def proc = cmd.execute(null, dir)
  proc.waitFor()
  if(proc.exitValue() != 0)
  {
    println('')
    println("\"${desc}\" failed")
    println('--------stdout---------')
    println(proc.in.text)
    println('--------stderr---------')
    println(proc.err.text)
    println('+---------------------+')
    println('|    build failed     |')
    println('+---------------------+')
    System.exit(-1)
  }
  println(' finished')

}

public boolean checkError(Process proc)
{
  if(proc.exitValue() != 0)
  {
    println('')
    println('--------stdout---------')
    println(proc.in.text)
    println('--------stderr---------')
    println(proc.err.text)
    println('-----------------------')
    return false
  }

  return true
}

public String context(String contextPath, String filePath)
{
  String result = 
    '<?xml version="1.0"  encoding="ISO-8859-1"?>\n' + 
    '<!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.mortbay.org/configure.dtd">\n' +
    '\n' +
    '<Configure class="org.mortbay.jetty.webapp.WebAppContext">\n' + 
    '<Set name=\"contextPath\">/' + contextPath + '</Set>\n' +
    '<Set name=\"resourceBase\">' + filePath + '</Set>\n' +
    '\n' + 
    '</Configure>\n';
  return result;
}


