using ProjectRelay.Gameplay;
using UnityEngine;

namespace ProjectRelay.Gameplay
{
    public class MissionAutoStarter : MonoBehaviour
    {
        [SerializeField] private DeliveryMissionController missionController;
        [SerializeField] private float delaySeconds = 0.3f;

        private void Start()
        {
            if (missionController == null)
                missionController = GetComponent<DeliveryMissionController>();

            if (missionController != null)
                Invoke(nameof(StartMissionSafe), delaySeconds);
        }

        private void StartMissionSafe()
        {
            missionController.StartMission();
        }
    }
}
