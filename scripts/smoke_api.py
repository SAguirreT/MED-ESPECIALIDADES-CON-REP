"""End-to-end checks against a running DEMO instance. Creates fictional QA records,
cancels its appointment and disables its catalog records; preserves the audit trail.
Usage: python scripts/smoke_api.py --base http://localhost:8080
"""
import argparse, json, uuid
from datetime import datetime, timedelta, timezone
from urllib.request import Request, urlopen
from urllib.error import HTTPError

parser=argparse.ArgumentParser()
parser.add_argument('--base',default='http://localhost:8080')
parser.add_argument('--output')
args=parser.parse_args()
checks=[]
def call(method,path,body=None,token=None,status=200):
    headers={'Content-Type':'application/json'}
    if token: headers['Authorization']='Bearer '+token
    req=Request(args.base+'/api'+path,data=json.dumps(body).encode() if body is not None else None,headers=headers,method=method)
    try:
        with urlopen(req,timeout=20) as res: code=res.status;data=json.load(res)
    except HTTPError as e: code=e.code;data=json.load(e)
    assert code==status,(method,path,code,status,data)
    checks.append({'method':method,'path':path,'status':code})
    return data

admin=call('POST','/auth/signin',{'email':'admin@aurora.demo','password':'AuroraAdmin2026!'})['accessToken']
reader=call('POST','/auth/signin',{'email':'consulta@aurora.demo','password':'AuroraConsulta2026!'})['accessToken']
moderator=call('POST','/auth/signin',{'email':'recepcion@aurora.demo','password':'AuroraRecepcion2026!'})['accessToken']
call('GET','/test/all')
call('GET','/test/user',status=401)
call('GET','/test/user',token='invalid.token.signature',status=401)
call('GET','/test/user',token=reader)
call('GET','/test/admin',token=reader,status=403)
call('GET','/test/admin',token=admin)
call('GET','/test/mod',token=moderator)
call('GET','/usuarios',token=moderator,status=403)
call('POST','/especialidades',{'nombre':'No permitido'},token=moderator,status=403)
suffix=uuid.uuid4().hex[:8]
day=(datetime.now(timezone(timedelta(hours=-5)))+timedelta(days=4)).date()
date=day.isoformat()
e=call('POST','/especialidades',{'nombre':'QA '+suffix,'descripcion':'Verificación ficticia'},admin,201)
room=call('POST','/consultorios',{'codigo':'QA-'+suffix,'nombre':'Sala QA','piso':'1','area':'Pruebas'},admin,201)
mdata={'nombre':'Profesional','apellido':'QA '+suffix,'cmp':'QA-'+suffix,'telefono':'','email':'','especialidadIds':[e['id']]}
m=call('POST','/medicos',mdata,admin,201)
call('POST','/medicos',mdata,admin,409)
pdata={'nombre':'Paciente','apellido':'QA '+suffix,'dni':str(int(suffix,16)%100000000).zfill(8),'fechaNacimiento':'1995-01-01','telefono':'','email':''}
p=call('POST','/pacientes',pdata,moderator,201)
schedule={'diaSemana':['LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO'][day.weekday()],'horaInicio':'08:00','horaFin':'10:00','duracionCitaMinutos':30,'consultorioId':room['id']}
h=call('POST',f"/horarios/medico/{m['id']}",schedule,admin,201)
call('POST',f"/horarios/medico/{m['id']}",schedule,admin,409)
call('PUT',f"/horarios/{h['id']}",{**schedule,'duracionCitaMinutos':0},admin,400)
slots=call('GET',f"/disponibilidad/medico/{m['id']}?fecha={date}",token=admin)
assert len(slots)==4 and all(s['disponible'] for s in slots)
cdata={'pacienteId':p['id'],'medicoId':m['id'],'especialidadId':e['id'],'horarioId':h['id'],'fecha':date,'horaInicio':'08:00','motivo':'Control ficticio QA'}
c=call('POST','/citas',cdata,moderator,201)
call('POST','/citas',cdata,moderator,409)
call('PATCH',f"/citas/{c['id']}/atender",token=moderator,status=409)
call('PATCH',f"/medicos/{m['id']}/estado?estado=false",token=admin,status=409)
block={'fecha':date,'horaInicio':'08:00','horaFin':'09:00','motivo':'Bloqueo QA'}
call('POST',f"/horarios/medico/{m['id']}/bloqueos",block,admin,409)
call('PUT',f"/citas/{c['id']}",{**cdata,'horaInicio':'08:30'},moderator)
call('PATCH',f"/citas/{c['id']}/cancelar",{'motivo':'Fin de prueba'},moderator)
b=call('POST',f"/horarios/medico/{m['id']}/bloqueos",block,admin,201)
slots=call('GET',f"/disponibilidad/medico/{m['id']}?fecha={date}",token=admin)
assert not slots[0]['disponible']
call('PATCH',f"/bloqueos/{b['id']}/cancelar",token=admin)
v=call('POST','/vacaciones',{'medicoId':m['id'],'fechaInicio':date,'fechaFin':date,'motivo':'Permiso QA'},admin,201)
assert all(not s['disponible'] for s in call('GET',f"/disponibilidad/medico/{m['id']}?fecha={date}",token=admin))
call('PATCH',f"/vacaciones/{v['id']}/cancelar",token=admin)
assert all(s['disponible'] for s in call('GET',f"/disponibilidad/medico/{m['id']}?fecha={date}",token=admin))
for kind,record in [('horarios',h),('medicos',m),('pacientes',p),('especialidades',e),('consultorios',room)]:
    call('PATCH',f"/{kind}/{record['id']}/estado?estado=false",token=admin)
audit=call('GET','/auditoria?size=100&tabla=citas',token=admin)
assert any(a['registroId']==c['id'] and a['accion']=='CANCELAR' for a in audit['content'])
call('GET','/reportes/medicos-por-especialidad',token=admin)
call('GET','/dashboard',token=admin)
report={'baseUrl':args.base,'passed':len(checks),'checks':checks,'patientId':p['id'],'appointmentId':c['id'],'note':'QA records retained inactive; appointment cancelled. No credentials or tokens in this report.'}
if args.output:
    from pathlib import Path
    Path(args.output).write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'passed':len(checks),'baseUrl':args.base,'patientId':p['id'],'appointmentId':c['id']},ensure_ascii=False))
