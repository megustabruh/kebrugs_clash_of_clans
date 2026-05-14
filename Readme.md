<!-- How to run in prod -->
update this field with new api key : 
/opt/coc-backend/.env
<!-- Then -->
cd /clash-of-clans/kebrugs_clash_of_clans/backend
./deploy/deploy.sh
<!-- To view logs -->
sudo journalctl -u coc-backend -f


<!-- How to run in local -->
./run.sh