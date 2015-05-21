public class RafaelMartinsDeSouza implements IAgent {
   private Robot[] robots = new Robot[3];


   public RafaelMartinsDeSouza() {
      for (int i = 0; i < robots.length; i++) {
         robots[i] = new Robot(i);
      }
   }

   @Override
   public void reset() {

   }

   @Override
   public void update(Model m) {
      Robot.setModel(m);

      robots[0].setOp(2);
      robots[1].setOp(1);
      robots[2].setOp(0);

      for (Robot robot : robots) {
         robot.update();
      }
   }

   public static float sq_dist(float x1, float y1, float x2, float y2) {
      return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
   }

   private static class Robot {
      private static Model model;
      private int index;
      private int opIndex;

      public Robot(int i) {
         index = i;
      }

      public static void setModel(Model m) {
         model = m;
      }


      public void update() {
         if (isEnemyAlive()) {
            if (energy() < enemyEnergy()) {
               if (targetOfBomb()) retreat();
               else stay();
            } else {
               moveInRangeOf(opxpos(), opypos());
               if (inRangeOf(opxpos(), opypos())) {
                  throwBomb(opxpos(), opypos());
               }
            }
         } else {
            moveInRangeOf(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);

            if (inRangeOf(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT)) {
               throwBomb(Model.XFLAG_OPPONENT, Model.YFLAG_OPPONENT);
            }
         }
      }

      private boolean targetOfBomb() {
         for(int i = 0; i < model.getBombCount(); i++) {
            float d = sq_dist(xpos(), ypos(), model.getBombTargetX(i), model.getBombTargetY(i));

            if (d <= Model.BLAST_RADIUS * Model.BLAST_RADIUS) return true;
         }

         return false;
      }

      private void retreat() {
         goTo(Model.XFLAG, Model.YFLAG);
      }

      private float opxpos() {
         return model.getXOpponent(opIndex);
      }

      private boolean isEnemyAlive() {
         return model.getEnergyOpponent(opIndex) >= 0;
      }

      private float enemyEnergy() {
         return model.getEnergyOpponent(opIndex);
      }

      private float energy() {
         return model.getEnergySelf(index);
      }

      private float opypos() {
         return model.getYOpponent(opIndex);
      }

      private boolean inRangeOf(float x, float y) {
         float dist2 = sq_dist(xpos(), ypos(), x, y);

         return dist2 <= (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS) * (Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS);
      }

      private void moveInRangeOf(float xtarget, float ytarget) {
         float dx = xtarget - xpos() ;
         float dy = ytarget - ypos() ;
         float dist2 = sq_dist(xpos(), ypos(), xtarget, ytarget);
         float dist = (float) Math.sqrt(dist2);

         float x = xtarget - ((Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS - 1) / dist) * dx;
         float y = ytarget - ((Model.MAX_THROW_RADIUS + Model.BLAST_RADIUS - 1) / dist) * dy;

         goTo(x, y);
      }

      public void setOp(int i) {
         opIndex = i;
      }

      private void goTo(float x, float y) {
         model.setDestination(index, x, y);
      }

      private void throwBomb(float x, float y) {
         model.throwBomb(index, x, y);
      }

      private float xpos() {
         return model.getX(index);
      }

      private float ypos() {
         return model.getY(index);
      }

      private void stay() {
         model.setDestination(index, xpos(), ypos());
      }
   }
}